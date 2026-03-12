package ru.nsu.melody_shift.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackDto {

    public String title;

    public String artist;

    public Integer durationSec;

    public String platformId;
}
