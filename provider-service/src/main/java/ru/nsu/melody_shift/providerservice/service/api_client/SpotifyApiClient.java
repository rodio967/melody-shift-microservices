package ru.nsu.melody_shift.providerservice.service.api_client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.nsu.melody_shift.common.dto.TrackDto;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component("spotify")
public class SpotifyApiClient implements MusicApiClient {

    @Override
    public List<TrackDto> searchTracks(String query, String artist, String accessToken) {
        log.info("Spotify search: query='{}', artist='{}'", query, artist);
        // TODO: вызвать реальное Spotify API
        return Collections.emptyList();
    }

    @Override
    public List<TrackDto> getPlaylistTracks(String playlistId, String accessToken) {
        log.info("Spotify get playlist: playlistId={}", playlistId);
        return Collections.emptyList();
    }

    @Override
    public String createPlaylist(String name, String accessToken) {
        log.info("Spotify create playlist: name='{}'", name);
        // TODO: реальное создание, вернуть ID плейлиста
        return "mock_spotify_playlist_id";
    }

    @Override
    public void addTrack(String playlistId, String trackId, String accessToken) {
        log.info("Spotify add track: playlistId={}, trackId={}", playlistId, trackId);
        // TODO: реальное добавление
    }
}