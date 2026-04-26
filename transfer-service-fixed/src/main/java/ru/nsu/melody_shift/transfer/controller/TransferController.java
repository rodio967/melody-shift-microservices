package ru.nsu.melody_shift.transfer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nsu.melody_shift.common.dto.PlaylistDto;
import ru.nsu.melody_shift.transfer.dto.request.CreateTransferRequest;
import ru.nsu.melody_shift.transfer.dto.response.TransferDetailDto;
import ru.nsu.melody_shift.transfer.dto.response.TransferProgressDto;
import ru.nsu.melody_shift.transfer.dto.response.TransferResponse;
import ru.nsu.melody_shift.transfer.dto.response.TransferSummaryDto;
import ru.nsu.melody_shift.transfer.service.TransferService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * Запустить перенос плейлиста.
     */
    @PostMapping
    public ResponseEntity<TransferResponse> create(
            @Valid @RequestBody CreateTransferRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        TransferResponse response = transferService.startTransfer(userId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Список трансферов пользователя.
     */
    @GetMapping
    public ResponseEntity<List<TransferSummaryDto>> getUserTransfers(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(transferService.getUserTransfers(userId, page, size));
    }

    /**
     * Детали трансфера с треками.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransferDetailDto> getTransfer(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(transferService.getTransfer(id, userId));
    }

    /**
     * Прогресс трансфера.
     */
    @GetMapping("/{id}/progress")
    public ResponseEntity<TransferProgressDto> getProgress(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(transferService.getProgress(id, userId));
    }

    /**
     * Отменить трансфер.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTransfer(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId
    ) {
        transferService.cancelTransfer(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Повторить неудавшиеся треки.
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retryFailed(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId
    ) {
        transferService.retryFailed(id, userId);
        return ResponseEntity.accepted().build();
    }

    /**
     * Получить плейлисты пользователя с платформы (проксирует в ProviderService).
     */
    @GetMapping("/playlists")
    public ResponseEntity<List<PlaylistDto>> getUserPlaylists(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam String provider
    ) {
        return ResponseEntity.ok(transferService.getUserPlaylists(userId, provider));
    }
}
