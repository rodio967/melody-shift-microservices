package ru.nsu.melody_shift.providerservice.service.api_client;

import ru.nsu.melody_shift.common.dto.PlaylistDto;
import ru.nsu.melody_shift.common.dto.TrackDto;
import java.util.List;

public interface MusicApiClient {

    // Поиск треков по названию и исполнителю
    List<TrackDto> searchTracks(String query, String artist, String accessToken);

    // Получить все треки из плейлиста
    List<TrackDto> getPlaylistTracks(String playlistId, String accessToken);

    // Создать пустой плейлист с заданным именем, вернуть его ID
    String createPlaylist(String name, String accessToken);

    // Добавить трек в плейлист
    void addTrack(String playlistId, String trackId, String accessToken);

    // получить список плейлистов
    List<PlaylistDto> getUserPlaylists(String accessToken);
}