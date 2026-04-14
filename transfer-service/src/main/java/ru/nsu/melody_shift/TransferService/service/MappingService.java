package ru.nsu.melody_shift.TransferService.service;

import org.springframework.stereotype.Service;
import ru.nsu.melody_shift.TransferService.client.ProviderClient;
import ru.nsu.melody_shift.common.dto.TrackDto;

import java.util.List;
import java.util.Optional;

@Service
public class MappingService {

    private final ProviderClient providerClient;

    public MappingService(ProviderClient providerClient) {
        this.providerClient = providerClient;
    }

    // Добавляем параметр userId и передаём его в search
    public Optional<String> findMatch(String provider, TrackDto track, String userId) {
        List<TrackDto> candidates = search(provider, track, userId);
        return candidates.stream()
                .filter(candidate -> isSimilar(candidate, track))
                .findFirst()
                .map(TrackDto::getPlatformId);
    }

    // Добавляем userId и передаём в providerClient
    private List<TrackDto> search(String provider, TrackDto track, String userId) {
        return providerClient.searchTracks(provider, track.getTitle(), track.getArtist(), userId);
    }

    private boolean isSimilar(TrackDto t1, TrackDto t2) {
        return safeEquals(t1.getArtist(), t2.getArtist())
                && similarity(t1.getTitle(), t2.getTitle()) > 0.8;
    }

    private boolean safeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return normalize(a).equals(normalize(b));
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase()
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("feat\\.|ft\\.", "")
                .trim();
    }

    private double similarity(String a, String b) {
        a = normalize(a);
        b = normalize(b);
        return a.equals(b) ? 1.0 : 0.0;
    }
}