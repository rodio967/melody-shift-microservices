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
@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    // GET /api/transfers?page=0&size=20
    @GetMapping
    public ResponseEntity<List<TransferSummaryDto>> getUserTransfers(@RequestHeader("X-User-Id") String userId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transferService.getUserTransfers(userId, page, size));
    }

    // DELETE /api/transfers/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTransfer(@PathVariable UUID id,
                                               @RequestHeader("X-User-Id") String userId) {
        transferService.cancelTransfer(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transfer> getTransfer(@PathVariable UUID id) {
        return ResponseEntity.ok(transferService.get(id));
    }

    // POST /api/transfers/{id}/retry
    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retryFailedTracks(@PathVariable UUID id,
                                                  @RequestHeader("X-User-Id") String userId) {
        transferService.retryFailed(id, userId);
        return ResponseEntity.accepted().build();
    }

    // GET /api/transfers/{id}/progress
    @GetMapping("/{id}/progress")
    public ResponseEntity<TransferProgressDto> getProgress(@PathVariable UUID id) {
        return ResponseEntity.ok(transferService.getProgress(id));
    }

    @PostMapping
    public ResponseEntity<TransferResponse> create(
            @RequestBody CreateTransferRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(transferService.startTransfer(userId, request));
    }

    @GetMapping("/playlists")
    public ResponseEntity<List<PlaylistDto>> getUserPlaylists(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam String provider) {
        return ResponseEntity.ok(transferService.getUserPlaylists(userId, provider));
    }
}