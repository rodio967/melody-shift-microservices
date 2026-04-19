package ru.nsu.melody_shift.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDto {
    private String id;          // Уникальный ID плейлиста на платформе
    private String name;        // Название плейлиста
    private String description; // Описание (если есть)
    private Integer tracksCount;// Количество треков в плейлисте
}