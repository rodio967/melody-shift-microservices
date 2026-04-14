package ru.nsu.melody_shift.TransferService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.melody_shift.TransferService.entity.TransferItem;

import java.util.List;
import java.util.UUID;

public interface TransferItemRepository extends JpaRepository<TransferItem, UUID> {

    List<TransferItem> findByTransfer_Id(UUID transferId);
}