package ru.nsu.melody_shift.providerservice.service.api_client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.nsu.melody_shift.common.dto.PlaylistDto;
import ru.nsu.melody_shift.common.dto.TrackDto;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component("youtube")

public class YoutubeApiClient implements MusicApiClient {

    @Override
    public List<TrackDto> searchTracks(String query, String artist, String accessToken) {
        log.info("YoutubeMusic search: query='{}', artist='{}'", query, artist);
        // TODO: вызвать реальное YoutubeMusic API
        return Collections.emptyList();
    }

    @Override
    public List<TrackDto> getPlaylistTracks(String playlistId, String accessToken) {
        log.info("YoutubeMusic get playlist: playlistId={}", playlistId);
        return Collections.emptyList();
    }

    @Override
    public String createPlaylist(String name, String accessToken) {
        log.info("YoutubeMusic create playlist: name='{}'", name);
        // TODO: реальное создание, вернуть ID плейлиста
        return "mock_youtube_playlist_id";
    }

    @Override
    public void addTrack(String playlistId, String trackId, String accessToken) {
        log.info("YoutubeMusic add track: playlistId={}, trackId={}", playlistId, trackId);
        // TODO: реальное добавление
    }

    @Override
    public List<PlaylistDto> getUserPlaylists(String accessToken) {

        return null;
    }
}
