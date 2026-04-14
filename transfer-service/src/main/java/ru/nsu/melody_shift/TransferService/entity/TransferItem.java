package ru.nsu.melody_shift.TransferService.entity;

import jakarta.persistence.*;
import ru.nsu.melody_shift.TransferService.entity.TransferItemStatus;
import java.util.UUID;

@Entity
@Table(name = "transfer_items")
public class TransferItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private Transfer transfer;

    private String sourceTrackId;
    private String sourceName;
    private String sourceArtist;

    private String targetTrackId;

    @Enumerated(EnumType.STRING)
    private TransferItemStatus status;

    // JPA
    protected TransferItem() {}

    // factory method (рекомендую)
    public static TransferItem create(
            Transfer transfer,
            String sourceTrackId,
            String sourceName,
            String sourceArtist
    ) {
        TransferItem item = new TransferItem();
        item.transfer = transfer;
        item.sourceTrackId = sourceTrackId;
        item.sourceName = sourceName;
        item.sourceArtist = sourceArtist;
        item.status = TransferItemStatus.PENDING;
        return item;
    }

    public void markFound(String targetTrackId) {
        this.targetTrackId = targetTrackId;
        this.status = TransferItemStatus.FOUND;
    }

    public void markNotFound() {
        this.status = TransferItemStatus.NOT_FOUND;
    }


    public UUID getId() {
        return id;
    }

    public Transfer getTransfer() {
        return transfer;
    }

    public String getSourceTrackId() {
        return sourceTrackId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceArtist() {
        return sourceArtist;
    }

    public String getTargetTrackId() {
        return targetTrackId;
    }

    public TransferItemStatus getStatus() {
        return status;
    }
}