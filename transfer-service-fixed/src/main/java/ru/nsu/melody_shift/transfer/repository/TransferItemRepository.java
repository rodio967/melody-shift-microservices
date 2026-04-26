package ru.nsu.melody_shift.transfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.melody_shift.transfer.entity.TransferItem;
import ru.nsu.melody_shift.transfer.enums.TransferItemStatus;

import java.util.List;
import java.util.UUID;

public interface TransferItemRepository extends JpaRepository<TransferItem, UUID> {
    List<TransferItem> findByTransferId(UUID transferId);
    List<TransferItem> findByTransferIdAndStatus(UUID transferId, TransferItemStatus status);
    long countByTransferId(UUID transferId);
    long countByTransferIdAndStatus(UUID transferId, TransferItemStatus status);
}
