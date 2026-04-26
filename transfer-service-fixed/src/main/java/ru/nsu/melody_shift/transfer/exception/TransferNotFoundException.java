package ru.nsu.melody_shift.transfer.exception;

import java.util.UUID;

public class TransferNotFoundException extends RuntimeException {
    public TransferNotFoundException(UUID transferId) {
        super("Transfer not found: " + transferId);
    }

    public TransferNotFoundException(UUID transferId, String userId) {
        super("Transfer " + transferId + " not found for user " + userId);
    }
}
