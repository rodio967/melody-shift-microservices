package ru.nsu.melody_shift.transfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTransferRequest {

    @NotBlank(message = "Source provider is required")
    private String sourceProvider;

    @NotBlank(message = "Target provider is required")
    private String targetProvider;

    @NotBlank(message = "Source playlist ID is required")
    private String sourcePlaylistId;

    private String playlistName;
}
