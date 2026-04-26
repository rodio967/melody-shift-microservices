package ru.nsu.melody_shift.TransferService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nsu.melody_shift.TransferService.dto.request.CreateTransferRequest;
import ru.nsu.melody_shift.TransferService.dto.response.TransferResponse;
import ru.nsu.melody_shift.TransferService.entity.Transfer;
import ru.nsu.melody_shift.TransferService.service.TransferService;
import ru.nsu.melody_shift.TransferService.dto.*;
import ru.nsu.melody_shift.common.dto.PlaylistDto;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления переносами плейлистов.
 * Все эндпоинты требуют заголовок X-User-Id (ID пользователя, подставленный API Gateway после проверки JWT).
 * Для авторизации также нужен JWT-токен в заголовке Authorization (Bearer токен).
 * Базовый путь: /api/transfers
 */
@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    // =========================== 1. Получение списка переносов пользователя (история) ===========================
    /**
     * GET /api/transfers?page=0&size=20
     *
     * Возвращает список переносов (краткую сводку) для авторизованного пользователя с пагинацией.
     * Используется на странице истории переносов.
     *
     * @param userId ID пользователя (автоматически подставляется Gateway из JWT)
     * @param page   номер страницы (начинается с 0), необязательный, по умолчанию 0
     * @param size   количество элементов на странице, необязательный, по умолчанию 20
     * @return список объектов TransferSummaryDto (id, sourceProvider, targetProvider, status, createdAt, ...)
     */
    @GetMapping
    public ResponseEntity<List<TransferSummaryDto>> getUserTransfers(@RequestHeader("X-User-Id") String userId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transferService.getUserTransfers(userId, page, size));
    }

    // =========================== 2. Отмена переноса (только если статус PENDING) ===========================
    /**
     * DELETE /api/transfers/{id}
     *
     * Отменяет перенос, если он ещё не начал обрабатываться (статус PENDING).
     * После отмены статус меняется на FAILED (или CANCELLED).
     *
     * @param id     UUID переноса
     * @param userId ID пользователя (из заголовка)
     * @return HTTP 204 No Content при успешной отмене
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTransfer(@PathVariable UUID id,
                                               @RequestHeader("X-User-Id") String userId) {
        transferService.cancelTransfer(id, userId);
        return ResponseEntity.noContent().build();
    }

    // =========================== 3. Получение полной информации о переносе ===========================
    /**
     * GET /api/transfers/{id}
     *
     * Возвращает полный объект Transfer (содержит все поля: sourceProvider, targetProvider, статус, даты и т.д.).
     * Используется, например, для отображения деталей после перезагрузки страницы.
     *
     * @param id UUID переноса
     * @return объект Transfer (статус, id, playlistId и т.п.)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transfer> getTransfer(@PathVariable UUID id) {
        return ResponseEntity.ok(transferService.get(id));
    }

    // =========================== 4. Повторная попытка переноса неудавшихся треков ===========================
    /**
     * POST /api/transfers/{id}/retry
     *
     * Запускает повторную попытку переноса для треков, которые ранее завершились ошибкой.
     * Работает только для переносов со статусом PARTIAL или FAILED.
     * Запуск асинхронный, сразу возвращается HTTP 202 Accepted.
     *
     * @param id     UUID переноса
     * @param userId ID пользователя (для проверки прав)
     * @return HTTP 202 Accepted
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retryFailedTracks(@PathVariable UUID id,
                                                  @RequestHeader("X-User-Id") String userId) {
        transferService.retryFailed(id, userId);
        return ResponseEntity.accepted().build();
    }

    // =========================== 5. Детальный прогресс переноса ===========================
    /**
     * GET /api/transfers/{id}/progress
     *
     * Возвращает детальную информацию о прогрессе переноса: количество обработанных треков,
     * успешные, неудачные, список треков с ошибками (если есть).
     * Фронтенд может опрашивать этот эндпоинт каждые 2-3 секунды, пока статус IN_PROGRESS.
     *
     * @param id UUID переноса
     * @return TransferProgressDto (transferId, status, totalTracks, processedTracks, successCount, failedCount, failedTracks)
     */
    @GetMapping("/{id}/progress")
    public ResponseEntity<TransferProgressDto> getProgress(@PathVariable UUID id) {
        return ResponseEntity.ok(transferService.getProgress(id));
    }

    // =========================== 6. Создание нового переноса ===========================
    /**
     * POST /api/transfers
     *
     * Создаёт задачу на перенос плейлиста. Тело запроса содержит sourceProvider, targetProvider, sourcePlaylistId.
     * Перенос запускается асинхронно, сразу возвращается TransferResponse с id и статусом PENDING.
     *
     * Пример тела запроса:
     * {
     *   "sourceProvider": "spotify",
     *   "targetProvider": "youtube",
     *   "sourcePlaylistId": "37i9dQZF1DXcBWIGoYBM5M"
     * }
     *
     * @param request CreateTransferRequest
     * @param userId  ID пользователя (из заголовка)
     * @return TransferResponse (transferId, status)
     */
    @PostMapping
    public ResponseEntity<TransferResponse> create(@RequestBody CreateTransferRequest request,
                                                   @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(transferService.startTransfer(userId, request));
    }

    // =========================== 7. Получение всех плейлистов пользователя у провайдера ===========================
    /**
     * GET /api/transfers/playlists?provider=spotify
     *
     * Возвращает список плейлистов текущего пользователя для указанного музыкального провайдера (spotify, youtube, deezer...).
     * Используется на первом шаге создания переноса: пользователь выбирает провайдера и ему показываются его плейлисты.
     *
     * @param userId   ID пользователя (из заголовка)
     * @param provider имя провайдера (spotify, youtube, deezer)
     * @return список PlaylistDto (id, name, description, tracksCount)
     */
    @GetMapping("/playlists")
    public ResponseEntity<List<PlaylistDto>> getUserPlaylists(@RequestHeader("X-User-Id") String userId,
                                                              @RequestParam String provider) {
        return ResponseEntity.ok(transferService.getUserPlaylists(userId, provider));
    }
}