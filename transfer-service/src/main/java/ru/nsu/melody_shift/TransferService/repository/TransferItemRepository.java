package ru.nsu.melody_shift.TransferService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.melody_shift.TransferService.entity.TransferItem;
import ru.nsu.melody_shift.TransferService.enums.TransferItemStatus;

import java.util.List;
import java.util.UUID;

public interface TransferItemRepository extends JpaRepository<TransferItem, UUID> {
    List<TransferItem> findByTransferIdAndStatus(UUID transferId, TransferItemStatus status);
    List<TransferItem> findByTransferId(UUID transferId);
    long countByTransferId(UUID transferId);
    long countByTransferIdAndStatus(UUID transferId, TransferItemStatus status);
}