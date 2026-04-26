package ru.nsu.melody_shift.transfer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import ru.nsu.melody_shift.transfer.enums.TransferStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
@Getter
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String sourceProvider;

    @Column(nullable = false)
    private String targetProvider;

    @Column(nullable = false)
    private String sourcePlaylistId;

    private String sourcePlaylistName;

    private String targetPlaylistId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    protected Transfer() {}

    public static Transfer create(
            String userId,
            String sourceProvider,
            String targetProvider,
            String sourcePlaylistId,
            String sourcePlaylistName
    ) {
        Transfer t = new Transfer();
        t.userId = userId;
        t.sourceProvider = sourceProvider;
        t.targetProvider = targetProvider;
        t.sourcePlaylistId = sourcePlaylistId;
        t.sourcePlaylistName = sourcePlaylistName;
        t.status = TransferStatus.PENDING;
        t.createdAt = Instant.now();
        t.updatedAt = Instant.now();
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

    public void markCancelled() {
        this.status = TransferStatus.CANCELLED;
        touch();
    }

    public void setTargetPlaylistId(String playlistId) {
        this.targetPlaylistId = playlistId;
        touch();
    }

    public boolean isCancellable() {
        return this.status == TransferStatus.PENDING;
    }

    public boolean isRetryable() {
        return this.status == TransferStatus.PARTIAL || this.status == TransferStatus.FAILED;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
