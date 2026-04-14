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
import ru.nsu.melody_shift.TransferService.enums.TransferStatus;
import ru.nsu.melody_shift.TransferService.repository.TransferRepository;

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

            String newPlaylistId = providerClient.createPlaylist(
                    transfer.getTargetProvider(),
                    "Transferred playlist",
                    transfer.getUserId()
            );

            int success = 0;
            for (var track : tracks) {
                try {
                    var result = mappingService.findMatch(transfer.getTargetProvider(), track, transfer.getUserId());
                    if (result.isPresent()) {
                        providerClient.addTrack(transfer.getTargetProvider(), newPlaylistId, result.get(), transfer.getUserId());
                        success++;
                    }
                } catch (Exception e) {
                    log.error("Failed to transfer track: {}", track, e);
                }
            }

            transfer.setTargetPlaylist(newPlaylistId);

            if (success == tracks.size()) {
                transfer.markCompleted();
                log.info("Transfer {} completed successfully", transferId);
            } else {
                transfer.markPartial();
                log.warn("Transfer {} completed partially: {}/{} tracks", transferId, success, tracks.size());
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
        // TODO переделать под повторную попытку отдельных треков
        processTransferAsync(transferId);
    }

    public TransferProgressDto getProgress(UUID transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
        // TODO сделать получение данных из transfer_items
        TransferProgressDto dto = new TransferProgressDto();
        dto.setTransferId(transfer.getId());
        dto.setStatus(transfer.getStatus().name());
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