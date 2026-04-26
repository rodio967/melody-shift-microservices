package ru.nsu.melody_shift.transfer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TransferDetailDto {
    private UUID id;
    private String sourceProvider;
    private String targetProvider;
    private String sourcePlaylistId;
    private String sourcePlaylistName;
    private String targetPlaylistId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<TransferItemDto> items;

    @Data
    @AllArgsConstructor
    public static class TransferItemDto {
        private UUID id;
        private String sourceTrackName;
        private String sourceArtist;
        private String targetTrackId;
        private String status;
        private String errorMessage;
    }
}
