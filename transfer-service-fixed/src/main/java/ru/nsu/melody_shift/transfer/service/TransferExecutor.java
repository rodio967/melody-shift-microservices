package ru.nsu.melody_shift.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.nsu.melody_shift.common.dto.PlaylistDto;
import ru.nsu.melody_shift.common.dto.TrackDto;
import ru.nsu.melody_shift.transfer.client.ProviderClient;
import ru.nsu.melody_shift.transfer.entity.Transfer;
import ru.nsu.melody_shift.transfer.entity.TransferItem;
import ru.nsu.melody_shift.transfer.enums.TransferItemStatus;
import ru.nsu.melody_shift.transfer.repository.TransferItemRepository;
import ru.nsu.melody_shift.transfer.repository.TransferRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Выполняет перенос треков асинхронно.
 * Вынесен в отдельный бин, чтобы @Async работал через Spring AOP-прокси.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferExecutor {

    private final TransferRepository transferRepository;
    private final TransferItemRepository transferItemRepository;
    private final ProviderClient providerClient;
    private final TrackMatcher trackMatcher;

    /**
     * Основной процесс переноса плейлиста.
     * Вызывается асинхронно из TransferService.
     *
     * @param transferId ID трансфера
     * @param userId     ID пользователя (передаём явно, т.к. в async-потоке нет RequestContext)
     */
    @Async
    public void executeTransfer(UUID transferId, String userId) {
        log.info("Starting transfer {}", transferId);

        try {
            Transfer transfer = transferRepository.findById(transferId)
                    .orElseThrow(() -> new RuntimeException("Transfer not found: " + transferId));

            transfer.markInProgress();
            transferRepository.save(transfer);

            // 1. Получаем треки из исходного плейлиста
            List<TrackDto> sourceTracks = providerClient.getPlaylistTracks(
                    transfer.getSourceProvider(),
                    transfer.getSourcePlaylistId(),
                    userId
            );

            if (sourceTracks == null || sourceTracks.isEmpty()) {
                log.warn("Transfer {} — source playlist is empty", transferId);
                transfer.markCompleted();
                transferRepository.save(transfer);
                return;
            }

            log.info("Transfer {} — found {} tracks in source playlist", transferId, sourceTracks.size());

            // 2. Создаём целевой плейлист
            String playlistName = transfer.getSourcePlaylistName() != null
                    ? transfer.getSourcePlaylistName()
                    : "Transferred playlist";

            PlaylistDto newPlaylist = providerClient.createPlaylist(
                    transfer.getTargetProvider(),
                    playlistName,
                    userId
            );
            transfer.setTargetPlaylistId(newPlaylist.getId());
            transferRepository.save(transfer);

            // 3. Создаём TransferItem для каждого трека
            List<TransferItem> items = sourceTracks.stream()
                    .map(track -> TransferItem.create(
                            transfer,
                            track.getPlatformId(),
                            track.getTitle(),
                            track.getArtist()
                    ))
                    .toList();
            transferItemRepository.saveAll(items);

            // 4. Ищем и переносим каждый трек
            int successCount = 0;
            for (TransferItem item : items) {
                successCount += processItem(item, transfer, userId);
            }

            // 5. Обновляем статус трансфера
            updateTransferStatus(transfer, successCount, items.size());

        } catch (Exception e) {
            log.error("Transfer {} failed with error: {}", transferId, e.getMessage(), e);
            transferRepository.findById(transferId).ifPresent(t -> {
                t.markFailed();
                transferRepository.save(t);
            });
        }
    }

    /**
     * Повторная попытка для неудавшихся треков.
     *
     * @param transferId ID трансфера
     * @param userId     ID пользователя
     */
    @Async
    public void executeRetry(UUID transferId, String userId) {
        log.info("Retrying failed tracks for transfer {}", transferId);

        try {
            Transfer transfer = transferRepository.findById(transferId)
                    .orElseThrow(() -> new RuntimeException("Transfer not found: " + transferId));

            List<TransferItem> failedItems = transferItemRepository
                    .findByTransferIdAndStatus(transferId, TransferItemStatus.FAILED);

            List<TransferItem> notFoundItems = transferItemRepository
                    .findByTransferIdAndStatus(transferId, TransferItemStatus.NOT_FOUND);

            failedItems.addAll(notFoundItems);

            if (failedItems.isEmpty()) {
                log.info("Transfer {} — no failed tracks to retry", transferId);
                return;
            }

            transfer.markInProgress();
            transferRepository.save(transfer);

            int successCount = 0;
            for (TransferItem item : failedItems) {
                successCount += processItem(item, transfer, userId);
            }

            // Пересчитываем общий статус
            long totalItems = transferItemRepository.countByTransferId(transferId);
            long totalSuccess = transferItemRepository.countByTransferIdAndStatus(
                    transferId, TransferItemStatus.SUCCESS);

            updateTransferStatus(transfer, (int) totalSuccess, (int) totalItems);

            log.info("Retry for transfer {} completed: {} new successes", transferId, successCount);

        } catch (Exception e) {
            log.error("Retry for transfer {} failed: {}", transferId, e.getMessage(), e);
            transferRepository.findById(transferId).ifPresent(t -> {
                t.markFailed();
                transferRepository.save(t);
            });
        }
    }

    /**
     * Обрабатывает один трек: ищет на целевой платформе и добавляет в плейлист.
     *
     * @return 1 если успешно, 0 если нет
     */
    private int processItem(TransferItem item, Transfer transfer, String userId) {
        try {
            TrackDto sourceTrack = new TrackDto();
            sourceTrack.setTitle(item.getSourceTrackName());
            sourceTrack.setArtist(item.getSourceArtist());

            Optional<String> matchedTrackId = trackMatcher.findMatch(
                    transfer.getTargetProvider(), sourceTrack, userId);

            if (matchedTrackId.isPresent()) {
                providerClient.addTrack(
                        transfer.getTargetProvider(),
                        transfer.getTargetPlaylistId(),
                        matchedTrackId.get(),
                        userId
                );
                item.markSuccess(matchedTrackId.get());
                transferItemRepository.save(item);
                return 1;
            } else {
                item.markNotFound();
                transferItemRepository.save(item);
                log.debug("No match found for '{}' by '{}'",
                        item.getSourceTrackName(), item.getSourceArtist());
                return 0;
            }
        } catch (Exception e) {
            item.markFailed(e.getMessage());
            transferItemRepository.save(item);
            log.warn("Failed to transfer track '{}': {}",
                    item.getSourceTrackName(), e.getMessage());
            return 0;
        }
    }

    private void updateTransferStatus(Transfer transfer, int successCount, int totalCount) {
        if (successCount == totalCount) {
            transfer.markCompleted();
            log.info("Transfer {} completed: {}/{} tracks", transfer.getId(), successCount, totalCount);
        } else if (successCount > 0) {
            transfer.markPartial();
            log.info("Transfer {} partial: {}/{} tracks", transfer.getId(), successCount, totalCount);
        } else {
            transfer.markFailed();
            log.warn("Transfer {} failed: 0/{} tracks", transfer.getId(), totalCount);
        }
        transferRepository.save(transfer);
    }
}
