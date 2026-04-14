package ru.nsu.melody_shift.TransferService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferSummaryDto {
    private UUID id;
    private String sourceProvider;
    private String targetProvider;
    private String status;
    private Instant createdAt;
    private int totalTracks;
    private int transferredTracks;
}