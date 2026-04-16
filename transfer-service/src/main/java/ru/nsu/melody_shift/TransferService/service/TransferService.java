package ru.nsu.melody_shift.TransferService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.melody_shift.TransferService.client.ProviderClient;
import ru.nsu.melody_shift.TransferService.dto.TransferProgressDto;
import ru.nsu.melody_shift.TransferService.dto.TransferSummaryDto;
import ru.nsu.melody_shift.TransferService.dto.request.CreateTransferRequest;
import ru.nsu.melody_shift.TransferService.dto.response.TransferResponse;
import ru.nsu.melody_shift.TransferService.entity.Transfer;
import ru.nsu.melody_shift.TransferService.entity.TransferItem;
import ru.nsu.melody_shift.TransferService.enums.TransferItemStatus;
import ru.nsu.melody_shift.TransferService.enums.TransferStatus;
import ru.nsu.melody_shift.TransferService.repository.TransferItemRepository;
import ru.nsu.melody_shift.TransferService.repository.TransferRepository;
import ru.nsu.melody_shift.common.dto.TrackDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final ProviderClient providerClient;
    private final MappingService mappingService;
    private final TransferItemRepository transferItemRepository;

    @Transactional
    public TransferResponse startTransfer(String userId, CreateTransferRequest request) {
        Transfer transfer = Transfer.create(
                userId,
                request.getSourceProvider(),
                request.getTargetProvider(),
                request.getSourcePlaylistId()
        );

        transfer = transferRepository.save(transfer);
        log.info("Transfer created with id: {}", transfer.getId());

        processTransferAsync(transfer.getId());

        return new TransferResponse(transfer.getId(), "PENDING");
    }

    @Async
    @Transactional
    public void processTransferAsync(UUID transferId) {
        try {
            Transfer transfer = transferRepository.findById(transferId)
                    .orElseThrow(() -> new RuntimeException("Transfer not found: " + transferId));

            transfer.markInProgress();
            transferRepository.save(transfer);
            log.info("Transfer {} started", transferId);

            var tracks = providerClient.getPlaylistTracks(
                    transfer.getSourceProvider(),
                    transfer.getSourcePlaylistId(),
                    transfer.getUserId()
            );

            if (tracks == null || tracks.isEmpty()) {
                log.warn("Transfer {} has no tracks", transferId);
                transfer.markCompleted();
                transferRepository.save(transfer);
                return;
            }

            // Создаём целевой плейлист
            String newPlaylistId = providerClient.createPlaylist(
                    transfer.getTargetProvider(),
                    "Transferred playlist",
                    transfer.getUserId()
            );
            transfer.setTargetPlaylist(newPlaylistId);
            transferRepository.save(transfer);

            int success = 0;
            // Создаём TransferItem для каждого трека
            for (TrackDto track : tracks) {
                TransferItem item = TransferItem.create(
                        transfer,
                        track.getPlatformId(),
                        track.getTitle(),
                        track.getArtist()
                );
                transferItemRepository.save(item);

                try {
                    var result = mappingService.findMatch(transfer.getTargetProvider(), track, transfer.getUserId());
                    if (result.isPresent()) {
                        providerClient.addTrack(transfer.getTargetProvider(), newPlaylistId, result.get(), transfer.getUserId());
                        item.setTargetTrackId(result.get());
                        item.setStatus(TransferItemStatus.SUCCESS);
                        success++;
                    } else {
                        item.setStatus(TransferItemStatus.FAILED);
                        item.setErrorMessage("No matching track found");
                    }
                } catch (Exception e) {
                    item.setStatus(TransferItemStatus.FAILED);
                    item.setErrorMessage(e.getMessage());
                    log.error("Failed to transfer track: {}", track, e);
                }
                transferItemRepository.save(item);
            }

            // Обновляем статус Transfer на основе успешности
            if (success == tracks.size()) {
                transfer.markCompleted();
                log.info("Transfer {} completed successfully", transferId);
            } else if (success > 0) {
                transfer.markPartial();
                log.warn("Transfer {} completed partially: {}/{} tracks", transferId, success, tracks.size());
            } else {
                transfer.markFailed();
            }
            transferRepository.save(transfer);

        } catch (Exception e) {
            log.error("Transfer {} failed with error", transferId, e);
            transferRepository.findById(transferId).ifPresent(transfer -> {
                transfer.markFailed();
                transferRepository.save(transfer);
            });
        }
    }
    public Transfer get(UUID id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found: " + id));
    }


    public List<TransferSummaryDto> getUserTransfers(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transfer> transfersPage = transferRepository.findByUserId(userId, pageable);
        return transfersPage.stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelTransfer(UUID transferId, String userId) {
        Transfer transfer = transferRepository.findByIdAndUserId(transferId, userId)
                .orElseThrow(() -> new RuntimeException("Transfer not found or access denied"));
        if (transfer.getStatus() == TransferStatus.PENDING) {
            transfer.markFailed();
            transferRepository.save(transfer);
            log.info("Transfer {} cancelled by user {}", transferId, userId);
        } else {
            throw new IllegalStateException("Cannot cancel transfer in status: " + transfer.getStatus());
        }
    }

    @Async
    @Transactional
    public void retryFailed(UUID transferId, String userId) {
        Transfer transfer = transferRepository.findByIdAndUserId(transferId, userId)
                .orElseThrow(() -> new RuntimeException("Transfer not found or access denied"));
        if (transfer.getStatus() != TransferStatus.PARTIAL && transfer.getStatus() != TransferStatus.FAILED) {
            throw new IllegalStateException("Retry only allowed for PARTIAL or FAILED transfers");
        }

        // Найти все неудавшиеся треки
        List<TransferItem> failedItems = transferItemRepository.findByTransferIdAndStatus(transferId, TransferItemStatus.FAILED);
        if (failedItems.isEmpty()) {
            log.info("No failed tracks to retry for transfer {}", transferId);
            return;
        }

        transfer.markInProgress();
        transferRepository.save(transfer);

        int success = 0;
        for (TransferItem item : failedItems) {
            try {
                TrackDto trackDto = new TrackDto();
                trackDto.setTitle(item.getSourceName());
                trackDto.setArtist(item.getSourceArtist());

                var result = mappingService.findMatch(transfer.getTargetProvider(), trackDto, userId);
                if (result.isPresent()) {
                    providerClient.addTrack(transfer.getTargetProvider(), transfer.getTargetPlaylistId(), result.get(), userId);
                    item.setTargetTrackId(result.get());
                    item.setStatus(TransferItemStatus.SUCCESS);
                    success++;
                } else {
                    item.setStatus(TransferItemStatus.FAILED);
                    item.setErrorMessage("No match found on retry");
                }
            } catch (Exception e) {
                item.setStatus(TransferItemStatus.FAILED);
                item.setErrorMessage(e.getMessage());
            }
            transferItemRepository.save(item);
        }

        long total = transferItemRepository.countByTransferId(transferId);
        long successful = transferItemRepository.countByTransferIdAndStatus(transferId, TransferItemStatus.SUCCESS);
        if (successful == total) {
            transfer.markCompleted();
        } else if (successful > 0) {
            transfer.markPartial();
        } else {
            transfer.markFailed();
        }
        transferRepository.save(transfer);
    }

    public TransferProgressDto getProgress(UUID transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
        List<TransferItem> items = transferItemRepository.findByTransferId(transferId);
        int total = items.size();
        int success = (int) items.stream().filter(i -> i.getStatus() == TransferItemStatus.SUCCESS).count();
        int failed = (int) items.stream().filter(i -> i.getStatus() == TransferItemStatus.FAILED).count();

        TransferProgressDto dto = new TransferProgressDto();
        dto.setTransferId(transfer.getId());
        dto.setStatus(transfer.getStatus().name());
        dto.setTotalTracks(total);
        dto.setSuccessCount(success);
        dto.setFailedCount(failed);
        dto.setProcessedTracks(success + failed);
        return dto;
    }

    private TransferSummaryDto toSummaryDto(Transfer transfer) {
        return new TransferSummaryDto(
                transfer.getId(),
                transfer.getSourceProvider(),
                transfer.getTargetProvider(),
                transfer.getStatus().name(),
                transfer.getCreatedAt(),
                0, 0
        );
    }
}