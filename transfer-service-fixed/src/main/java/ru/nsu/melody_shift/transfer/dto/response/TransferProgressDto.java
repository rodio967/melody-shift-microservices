package ru.nsu.melody_shift.transfer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TransferProgressDto {
    private UUID transferId;
    private String status;
    private int totalTracks;
    private int processedTracks;
    private int successCount;
    private int notFoundCount;
    private int failedCount;
}
