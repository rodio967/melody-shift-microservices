package ru.nsu.melody_shift.providerservice.service.api_client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.nsu.melody_shift.common.dto.PlaylistDto;
import ru.nsu.melody_shift.common.dto.TrackDto;
import ru.nsu.melody_shift.providerservice.client.SpotifyApiFeignClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component("spotify")
public class SpotifyApiClient implements MusicApiClient {

    private final SpotifyApiFeignClient spotifyApiFeignClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public SpotifyApiClient(SpotifyApiFeignClient spotifyApiFeignClient) {
        this.spotifyApiFeignClient = spotifyApiFeignClient;
    }

    @Override
    public List<TrackDto> searchTracks(String query, String artist, String accessToken) {
        log.info("Spotify search: query='{}', artist='{}'", query, artist);

        // 1. Формируем строку запроса для Spotify API
        String searchQuery = String.format("track:%s artist:%s", query, artist);
        String authHeader = "Bearer " + accessToken;

        try {
            // 2. Вызываем Feign-клиента
            String response = spotifyApiFeignClient.searchTracks(searchQuery, "track", 5, authHeader);

            // 3. Парсим JSON-ответ
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("tracks").path("items");

            List<TrackDto> tracks = new ArrayList<>();
            for (JsonNode item : items) {
                String title = item.path("name").asText();
                String platformId = item.path("id").asText();
                Integer durationSec = item.path("duration_ms").asInt() / 1000;

                // Извлекаем имя первого исполнителя
                String artistName = "";
                if (item.path("artists").isArray() && item.path("artists").size() > 0) {
                    artistName = item.path("artists").get(0).path("name").asText();
                }

                TrackDto trackDto = new TrackDto(title, artistName, durationSec, platformId);
                tracks.add(trackDto);
            }
            return tracks;
        } catch (Exception e) {
            log.error("Failed to search tracks on Spotify", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<TrackDto> getPlaylistTracks(String playlistId, String accessToken) {
        log.info("Spotify get playlist: playlistId={}", playlistId);
        String authHeader = "Bearer " + accessToken;
        List<TrackDto> allTracks = new ArrayList<>();

        try {
            String response = spotifyApiFeignClient.getPlaylistTracks(playlistId, authHeader);
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");

            for (JsonNode item : items) {
                JsonNode trackNode = item.path("track");
                String title = trackNode.path("name").asText();
                String platformId = trackNode.path("id").asText();
                Integer durationSec = trackNode.path("duration_ms").asInt() / 1000;

                String artistName = "";
                if (trackNode.path("artists").isArray() && trackNode.path("artists").size() > 0) {
                    artistName = trackNode.path("artists").get(0).path("name").asText();
                }

                TrackDto trackDto = new TrackDto(title, artistName, durationSec, platformId);
                allTracks.add(trackDto);
            }
            // TODO: Обработать пагинацию, если треков > 100
            return allTracks;
        } catch (Exception e) {
            log.error("Failed to get playlist tracks from Spotify", e);
            return Collections.emptyList();
        }
    }

    @Override
    public String createPlaylist(String name, String accessToken) {
        log.info("Spotify create playlist: name='{}'", name);
        String authHeader = "Bearer " + accessToken;

        try {
            String requestBody = String.format("{\"name\":\"%s\", \"public\":false}", name);
            // Здесь нужен user ID. Его можно получить из Spotify API, но пока используем строку "me"
            String response = spotifyApiFeignClient.createPlaylist("me", requestBody, authHeader);

            JsonNode root = objectMapper.readTree(response);
            String playlistId = root.path("id").asText();
            log.info("Spotify playlist created with ID: {}", playlistId);
            return playlistId;
        } catch (Exception e) {
            log.error("Failed to create playlist on Spotify", e);
            throw new RuntimeException("Could not create Spotify playlist", e);
        }
    }

    @Override
    public void addTrack(String playlistId, String trackId, String accessToken) {
        log.info("Spotify add track: playlistId={}, trackId={}", playlistId, trackId);
        String authHeader = "Bearer " + accessToken;

        try {
            String spotifyUri = "spotify:track:" + trackId;
            String requestBody = String.format("{\"uris\":[\"%s\"]}", spotifyUri);
            spotifyApiFeignClient.addTrackToPlaylist(playlistId, requestBody, authHeader);
            log.info("Track {} added to playlist {}", trackId, playlistId);
        } catch (Exception e) {
            log.error("Failed to add track to Spotify playlist", e);
            throw new RuntimeException("Could not add track to Spotify playlist", e);
        }
    }

    @Override
    public List<PlaylistDto> getUserPlaylists(String accessToken) {
        log.info("Fetching Spotify playlists for user");
        String authHeader = "Bearer " + accessToken;

        try {
            String response = spotifyApiFeignClient.getUserPlaylists(authHeader);
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");

            List<PlaylistDto> playlists = new ArrayList<>();
            for (JsonNode item : items) {
                PlaylistDto dto = new PlaylistDto();
                dto.setId(item.path("id").asText());
                dto.setName(item.path("name").asText());
                dto.setDescription(item.path("description").asText());
                dto.setTracksCount(item.path("tracks").path("total").asInt());
                playlists.add(dto);
            }
            return playlists;
        } catch (Exception e) {
            log.error("Failed to fetch Spotify playlists", e);
            return Collections.emptyList();
        }
    }
}