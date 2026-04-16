package ru.nsu.melody_shift.providerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.melody_shift.common.dto.TrackDto;
import ru.nsu.melody_shift.providerservice.client.UserServiceClient;
import ru.nsu.melody_shift.common.dto.OAuthTokenDto;
import ru.nsu.melody_shift.providerservice.service.api_client.MusicApiClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderService {

    private final UserServiceClient userServiceClient;
    private final Map<String, MusicApiClient> musicApiClients;

    @Value("${internal.secret}")
    private String internalSecret;

    // Поиск треков
    public List<TrackDto> searchTracks(Long userId, String provider, String query, String artist) {
        String token = getAccessToken(userId, provider);
        MusicApiClient client = getClient(provider);
        return client.searchTracks(query, artist, token);
    }

    // Получить треки плейлиста
    public List<TrackDto> getPlaylistTracks(Long userId, String provider, String playlistId) {
        String token = getAccessToken(userId, provider);
        MusicApiClient client = getClient(provider);
        return client.getPlaylistTracks(playlistId, token);
    }

    // Создать плейлист
    public String createPlaylist(Long userId, String provider, String name) {
        String token = getAccessToken(userId, provider);
        MusicApiClient client = getClient(provider);
        return client.createPlaylist(name, token);
    }

    // Добавить трек в плейлист
    public void addTrack(Long userId, String provider, String playlistId, String trackId) {
        String token = getAccessToken(userId, provider);
        MusicApiClient client = getClient(provider);
        client.addTrack(playlistId, trackId, token);
    }

    // Получить access token через UserService
    private String getAccessToken(Long userId, String provider) {
        OAuthTokenDto tokenDto = userServiceClient.getUserToken(userId, provider, internalSecret);
        if (tokenDto == null || tokenDto.getAccessToken() == null) {
            throw new RuntimeException("No access token for user " + userId + " and provider " + provider);
        }
        return tokenDto.getAccessToken();
    }

    // Выбрать реализацию MusicApiClient по имени провайдера
    private MusicApiClient getClient(String provider) {
        MusicApiClient client = musicApiClients.get(provider.toLowerCase());
        if (client == null) {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
        return client;
    }
}