package ru.nsu.melody_shift.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackDto {

    private String title;

    private String artist;

    private Integer durationSec;

    private String platformId;
}
