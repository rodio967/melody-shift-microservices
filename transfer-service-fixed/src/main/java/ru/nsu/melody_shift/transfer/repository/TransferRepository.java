package ru.nsu.melody_shift.transfer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.melody_shift.transfer.entity.Transfer;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    Page<Transfer> findByUserId(String userId, Pageable pageable);
    Optional<Transfer> findByIdAndUserId(UUID id, String userId);
}
