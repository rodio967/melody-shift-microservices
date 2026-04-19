package ru.nsu.melody_shift.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDto {
    private String id;
    private String name;
    private String description;
    private Integer tracksCount;
}