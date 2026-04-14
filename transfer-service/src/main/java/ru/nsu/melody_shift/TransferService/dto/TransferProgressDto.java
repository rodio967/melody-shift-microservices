package ru.nsu.melody_shift.TransferService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferProgressDto {
    private UUID transferId;
    private String status;
    private int totalTracks;
    private int processedTracks;
    private int successCount;
    private int failedCount;
}