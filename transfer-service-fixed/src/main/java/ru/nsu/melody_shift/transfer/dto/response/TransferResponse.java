package ru.nsu.melody_shift.transfer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TransferResponse {
    private UUID transferId;
    private String status;
}
