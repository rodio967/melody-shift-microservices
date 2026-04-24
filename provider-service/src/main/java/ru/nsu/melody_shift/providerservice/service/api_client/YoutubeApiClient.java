package ru.nsu.melody_shift.providerservice.service.api_client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.nsu.melody_shift.common.dto.PlaylistDto;
import ru.nsu.melody_shift.common.dto.TrackDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component("youtube")
public class YoutubeApiClient implements MusicApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${providers.youtube.api-url:https://www.googleapis.com/youtube/v3}")
    private String apiUrl;

    @Override
    public List<TrackDto> searchTracks(String query, String artist, String accessToken) {
        log.info("YouTube search: query='{}', artist='{}'", query, artist);
        String searchQuery = String.format("%s %s", query, artist);
        String url = apiUrl + "/search?part=snippet&q=" + urlEncode(searchQuery) + "&type=video&maxResults=10";

        String response = executeGet(url, accessToken);
        if (response == null) return Collections.emptyList();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");
            List<TrackDto> tracks = new ArrayList<>();
            for (JsonNode item : items) {
                JsonNode snippet = item.path("snippet");
                String title = snippet.path("title").asText();
                String videoId = item.path("id").path("videoId").asText();
                String channelTitle = snippet.path("channelTitle").asText();
                // YouTube API не возвращает длительность в поиске
                tracks.add(new TrackDto(title, channelTitle, 0, videoId));
            }
            return tracks;
        } catch (Exception e) {
            log.error("Failed to parse YouTube search response", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<TrackDto> getPlaylistTracks(String playlistId, String accessToken) {
        log.info("YouTube get playlist tracks: playlistId={}", playlistId);
        String url = apiUrl + "/playlistItems?part=snippet&playlistId=" + playlistId + "&maxResults=50";

        String response = executeGet(url, accessToken);
        if (response == null) return Collections.emptyList();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");
            List<TrackDto> tracks = new ArrayList<>();
            for (JsonNode item : items) {
                JsonNode snippet = item.path("snippet");
                String title = snippet.path("title").asText();
                String videoId = snippet.path("resourceId").path("videoId").asText();
                String channelTitle = snippet.path("channelTitle").asText();
                tracks.add(new TrackDto(title, channelTitle, 0, videoId));
            }
            return tracks;
        } catch (Exception e) {
            log.error("Failed to parse YouTube playlist tracks", e);
            return Collections.emptyList();
        }
    }

    @Override
    public String createPlaylist(String name, String accessToken) {
        log.info("YouTube create playlist: name='{}'", name);
        String url = apiUrl + "/playlists?part=snippet";
        String body = String.format("{\"snippet\":{\"title\":\"%s\",\"description\":\"Transferred from Melody Shift\"}}", escapeJson(name));

        String response = executePost(url, body, accessToken);
        if (response == null) return null;

        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("id").asText();
        } catch (Exception e) {
            log.error("Failed to parse YouTube create playlist response", e);
            return null;
        }
    }

    @Override
    public void addTrack(String playlistId, String trackId, String accessToken) {
        log.info("YouTube add track: playlistId={}, trackId={}", playlistId, trackId);
        String url = apiUrl + "/playlistItems?part=snippet";
        String body = String.format(
                "{\"snippet\":{\"playlistId\":\"%s\",\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"%s\"}}}",
                playlistId, trackId);
        executePost(url, body, accessToken);
    }

    @Override
    public List<PlaylistDto> getUserPlaylists(String accessToken) {
        log.info("YouTube get user playlists");
        String url = apiUrl + "/playlists?part=snippet&mine=true&maxResults=50";

        String response = executeGet(url, accessToken);
        if (response == null) return Collections.emptyList();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");
            List<PlaylistDto> playlists = new ArrayList<>();
            for (JsonNode item : items) {
                JsonNode snippet = item.path("snippet");
                PlaylistDto dto = new PlaylistDto();
                dto.setId(item.path("id").asText());
                dto.setName(snippet.path("title").asText());
                dto.setDescription(snippet.path("description").asText());
                // YouTube API не возвращает количество треков в этом запросе, нужен дополнительный вызов
                dto.setTracksCount(0);
                playlists.add(dto);
            }
            return playlists;
        } catch (Exception e) {
            log.error("Failed to parse YouTube playlists", e);
            return Collections.emptyList();
        }
    }

    // ========== Вспомогательные методы ==========

    private String executeGet(String url, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("YouTube API GET error: {} - {}", response.getStatusCode(), response.getBody());
                return null;
            }
        } catch (Exception e) {
            log.error("YouTube GET request failed: {}", url, e);
            return null;
        }
    }

    private String executePost(String url, String body, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("YouTube API POST error: {} - {}", response.getStatusCode(), response.getBody());
                return null;
            }
        } catch (Exception e) {
            log.error("YouTube POST request failed: {}", url, e);
            return null;
        }
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    private String escapeJson(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}