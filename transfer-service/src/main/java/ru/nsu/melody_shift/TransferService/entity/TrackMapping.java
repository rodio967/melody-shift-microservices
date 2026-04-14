package ru.nsu.melody_shift.TransferService.entity;

import jakarta.persistence.*;
import ru.nsu.melody_shift.TransferService.enums.MappingStatus;

import java.util.UUID;

@Entity
@Table(name = "track_mappings")
public class TrackMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID transferId;

    private String sourceTrackId;
    private String sourceName;
    private String sourceArtist;

    private String targetTrackId;

    @Enumerated(EnumType.STRING)
    private MappingStatus status;
}