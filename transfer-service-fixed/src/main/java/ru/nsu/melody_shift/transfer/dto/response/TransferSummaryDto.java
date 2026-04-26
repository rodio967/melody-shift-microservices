package ru.nsu.melody_shift.transfer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TransferSummaryDto {
    private UUID id;
    private String sourceProvider;
    private String targetProvider;
    private String sourcePlaylistName;
    private String status;
    private Instant createdAt;
    private int totalTracks;
    private int transferredTracks;
}
