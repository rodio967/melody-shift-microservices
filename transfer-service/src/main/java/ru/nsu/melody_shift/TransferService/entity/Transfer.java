package ru.nsu.melody_shift.TransferService.entity;

import jakarta.persistence.*;
import ru.nsu.melody_shift.TransferService.enums.TransferStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userId;

    private String sourceProvider;
    private String targetProvider;

    private String sourcePlaylistId;
    private String targetPlaylistId;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    public Transfer() {}

    // 🔹 фабричный метод
    public static Transfer create(
            String userId,
            String sourceProvider,
            String targetProvider,
            String sourcePlaylistId
    ) {
        Transfer t = new Transfer();
        t.userId = userId;
        t.sourceProvider = sourceProvider;
        t.targetProvider = targetProvider;
        t.sourcePlaylistId = sourcePlaylistId;
        t.touch();
        return t;
    }


    public void markInProgress() {
        this.status = TransferStatus.IN_PROGRESS;
        touch();
    }

    public void markCompleted() {
        this.status = TransferStatus.COMPLETED;
        touch();
    }

    public void markFailed() {
        this.status = TransferStatus.FAILED;
        touch();
    }

    public void markPartial() {
        this.status = TransferStatus.PARTIAL;
        touch();
    }

    public void setTargetPlaylist(String playlistId) {
        this.targetPlaylistId = playlistId;
        touch();
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getSourceProvider() {
        return sourceProvider;
    }

    public String getTargetProvider() {
        return targetProvider;
    }

    public String getSourcePlaylistId() {
        return sourcePlaylistId;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public String getTargetPlaylistId() {
        return targetPlaylistId;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}