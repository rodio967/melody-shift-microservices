package ru.nsu.melody_shift.TransferService.dto.request;

import lombok.Data;

@Data
public class CreateTransferRequest {
    private String sourceProvider;
    private String targetProvider;
    private String sourcePlaylistId;
}