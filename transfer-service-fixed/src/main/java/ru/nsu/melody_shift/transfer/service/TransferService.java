package ru.nsu.melody_shift.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.melody_shift.common.dto.PlaylistDto;
import ru.nsu.melody_shift.transfer.client.ProviderClient;
import ru.nsu.melody_shift.transfer.dto.request.CreateTransferRequest;
import ru.nsu.melody_shift.transfer.dto.response.TransferDetailDto;
import ru.nsu.melody_shift.transfer.dto.response.TransferProgressDto;
import ru.nsu.melody_shift.transfer.dto.response.TransferResponse;
import ru.nsu.melody_shift.transfer.dto.response.TransferSummaryDto;
import ru.nsu.melody_shift.transfer.entity.Transfer;
import ru.nsu.melody_shift.transfer.entity.TransferItem;
import ru.nsu.melody_shift.transfer.enums.TransferItemStatus;
import ru.nsu.melody_shift.transfer.exception.TransferNotFoundException;
import ru.nsu.melody_shift.transfer.repository.TransferItemRepository;
import ru.nsu.melody_shift.transfer.repository.TransferRepository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final TransferItemRepository transferItemRepository;
    private final TransferExecutor transferExecutor;
    private final ProviderClient providerClient;

    /**
     * Создаёт трансфер и запускает асинхронную обработку.
     */
    @Transactional
    public TransferResponse startTransfer(String userId, CreateTransferRequest request) {
        if (request.getSourceProvider().equalsIgnoreCase(request.getTargetProvider())) {
            throw new IllegalStateException("Source and target providers cannot be the same");
        }

        Transfer transfer = Transfer.create(
                userId,
                request.getSourceProvider().toLowerCase(),
                request.getTargetProvider().toLowerCase(),
                request.getSourcePlaylistId(),
                request.getPlaylistName()
        );

        transfer = transferRepository.save(transfer);
        log.info("Transfer {} created: {} -> {}",
                transfer.getId(), request.getSourceProvider(), request.getTargetProvider());

        UUID transferId = transfer.getId();

        // Запускаем async ПОСЛЕ коммита транзакции
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        transferExecutor.executeTransfer(transferId, userId);
                    }
                }
        );

        return new TransferResponse(transfer.getId(), transfer.getStatus().name());
    }

    /**
     * Получить детали трансфера с треками.
     */
    public TransferDetailDto getTransfer(UUID transferId, String userId) {
        Transfer transfer = findUserTransfer(transferId, userId);
        List<TransferItem> items = transferItemRepository.findByTransferId(transferId);

        List<TransferDetailDto.TransferItemDto> itemDtos = items.stream()
                .map(item -> new TransferDetailDto.TransferItemDto(
                        item.getId(),
                        item.getSourceTrackName(),
                        item.getSourceArtist(),
                        item.getTargetTrackId(),
                        item.getStatus().name(),
                        item.getErrorMessage()
                ))
                .collect(Collectors.toList());

        return new TransferDetailDto(
                transfer.getId(),
                transfer.getSourceProvider(),
                transfer.getTargetProvider(),
                transfer.getSourcePlaylistId(),
                transfer.getSourcePlaylistName(),
                transfer.getTargetPlaylistId(),
                transfer.getStatus().name(),
                transfer.getCreatedAt(),
                transfer.getUpdatedAt(),
                itemDtos
        );
    }

    /**
     * Получить прогресс трансфера.
     */
    public TransferProgressDto getProgress(UUID transferId, String userId) {
        Transfer transfer = findUserTransfer(transferId, userId);
        List<TransferItem> items = transferItemRepository.findByTransferId(transferId);

        int total = items.size();
        int success = 0;
        int notFound = 0;
        int failed = 0;

        for (TransferItem item : items) {
            switch (item.getStatus()) {
                case SUCCESS -> success++;
                case NOT_FOUND -> notFound++;
                case FAILED -> failed++;
            }
        }

        return new TransferProgressDto(
                transfer.getId(),
                transfer.getStatus().name(),
                total,
                success + notFound + failed,
                success,
                notFound,
                failed
        );
    }

    /**
     * Список трансферов пользователя с пагинацией.
     */
    public List<TransferSummaryDto> getUserTransfers(String userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return transferRepository.findByUserId(userId, pageable).stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Отмена трансфера (только в статусе PENDING).
     */
    @Transactional
    public void cancelTransfer(UUID transferId, String userId) {
        Transfer transfer = findUserTransfer(transferId, userId);

        if (!transfer.isCancellable()) {
            throw new IllegalStateException(
                    "Cannot cancel transfer in status: " + transfer.getStatus());
        }

        transfer.markCancelled();
        transferRepository.save(transfer);
        log.info("Transfer {} cancelled by user {}", transferId, userId);
    }

    /**
     * Повторная попытка для неудавшихся треков.
     * Валидация — синхронно, обработка — асинхронно.
     */
    public void retryFailed(UUID transferId, String userId) {
        Transfer transfer = findUserTransfer(transferId, userId);

        if (!transfer.isRetryable()) {
            throw new IllegalStateException(
                    "Retry only allowed for PARTIAL or FAILED transfers, current: " + transfer.getStatus());
        }

        // Валидация прошла — запускаем async в другом бине
        transferExecutor.executeRetry(transferId, userId);
        log.info("Retry started for transfer {} by user {}", transferId, userId);
    }

    /**
     * Получить плейлисты пользователя с платформы (проксирует в ProviderService).
     */
    public List<PlaylistDto> getUserPlaylists(String userId, String provider) {
        log.info("Fetching playlists for user {} from {}", userId, provider);
        return providerClient.getUserPlaylists(provider.toLowerCase(), userId);
    }

    // --- Private helpers ---

    private Transfer findUserTransfer(UUID transferId, String userId) {
        return transferRepository.findByIdAndUserId(transferId, userId)
                .orElseThrow(() -> new TransferNotFoundException(transferId, userId));
    }

    private TransferSummaryDto toSummaryDto(Transfer transfer) {
        long total = transferItemRepository.countByTransferId(transfer.getId());
        long success = transferItemRepository.countByTransferIdAndStatus(
                transfer.getId(), TransferItemStatus.SUCCESS);

        return new TransferSummaryDto(
                transfer.getId(),
                transfer.getSourceProvider(),
                transfer.getTargetProvider(),
                transfer.getSourcePlaylistName(),
                transfer.getStatus().name(),
                transfer.getCreatedAt(),
                (int) total,
                (int) success
        );
    }
}
