package ru.nsu.melody_shift.providerservice.service.api_client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.nsu.melody_shift.common.dto.PlaylistDto;
import ru.nsu.melody_shift.common.dto.TrackDto;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component("yandex")
public class YandexApiClient implements MusicApiClient {

    @Override
    public List<TrackDto> searchTracks(String query, String artist, String accessToken) {
        log.info("YandexMusic search: query='{}', artist='{}'", query, artist);
        // TODO: вызвать реальное YandexMusic API
        return Collections.emptyList();
    }

    @Override
    public List<TrackDto> getPlaylistTracks(String playlistId, String accessToken) {
        log.info("YandexMusic get playlist: playlistId={}", playlistId);
        return Collections.emptyList();
    }

    @Override
    public String createPlaylist(String name, String accessToken) {
        log.info("YandexMusic create playlist: name='{}'", name);
        // TODO: реальное создание, вернуть ID плейлиста
        return "mock_yandex_playlist_id";
    }

    @Override
    public void addTrack(String playlistId, String trackId, String accessToken) {
        log.info("YandexMusic add track: playlistId={}, trackId={}", playlistId, trackId);
        // TODO: реальное добавление
    }

    @Override
    public List<PlaylistDto> getUserPlaylists(String accessToken) {

        return null;
    }
}
