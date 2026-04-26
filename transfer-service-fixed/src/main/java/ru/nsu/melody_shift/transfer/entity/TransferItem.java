package ru.nsu.melody_shift.transfer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import ru.nsu.melody_shift.transfer.enums.TransferItemStatus;

import java.util.UUID;

@Entity
@Table(name = "transfer_items")
@Getter
public class TransferItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private Transfer transfer;

    @Column(nullable = false)
    private String sourceTrackId;

    @Column(nullable = false)
    private String sourceTrackName;

    @Column(nullable = false)
    private String sourceArtist;

    private String targetTrackId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferItemStatus status;

    private String errorMessage;

    protected TransferItem() {}

    public static TransferItem create(
            Transfer transfer,
            String sourceTrackId,
            String sourceTrackName,
            String sourceArtist
    ) {
        TransferItem item = new TransferItem();
        item.transfer = transfer;
        item.sourceTrackId = sourceTrackId;
        item.sourceTrackName = sourceTrackName;
        item.sourceArtist = sourceArtist;
        item.status = TransferItemStatus.PENDING;
        return item;
    }

    public void markSuccess(String targetTrackId) {
        this.targetTrackId = targetTrackId;
        this.status = TransferItemStatus.SUCCESS;
        this.errorMessage = null;
    }

    public void markNotFound() {
        this.status = TransferItemStatus.NOT_FOUND;
        this.errorMessage = "No matching track found";
    }

    public void markFailed(String error) {
        this.status = TransferItemStatus.FAILED;
        this.errorMessage = error;
    }
}
