package ru.nsu.melody_shift.providerservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nsu.melody_shift.common.dto.PlaylistDto;
import ru.nsu.melody_shift.common.dto.TrackDto;
import ru.nsu.melody_shift.providerservice.service.ProviderService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @GetMapping("/{provider}/search")
    public ResponseEntity<List<TrackDto>> searchTracks(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String provider,
            @RequestParam String query,
            @RequestParam String artist) {
        log.info("Search: userId={}, provider={}, query={}, artist={}", userId, provider, query, artist);
        return ResponseEntity.ok(providerService.searchTracks(userId, provider, query, artist));
    }

    @GetMapping("/{provider}/playlists/{playlistId}/tracks")
    public ResponseEntity<List<TrackDto>> getPlaylistTracks(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String provider,
            @PathVariable String playlistId) {
        log.info("Get playlist tracks: userId={}, provider={}, playlistId={}", userId, provider, playlistId);
        return ResponseEntity.ok(providerService.getPlaylistTracks(userId, provider, playlistId));
    }

    @PostMapping("/{provider}/playlists")
    public ResponseEntity<String> createPlaylist(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String provider,
            @RequestParam String name) {
        log.info("Create playlist: userId={}, provider={}, name={}", userId, provider, name);
        String playlistId = providerService.createPlaylist(userId, provider, name);
        return ResponseEntity.ok(playlistId);
    }

    @PostMapping("/{provider}/playlists/{playlistId}/tracks")
    public ResponseEntity<Void> addTrack(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String provider,
            @PathVariable String playlistId,
            @RequestParam String trackId) {
        log.info("Add track: userId={}, provider={}, playlistId={}, trackId={}", userId, provider, playlistId, trackId);
        providerService.addTrack(userId, provider, playlistId, trackId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{provider}/playlists")
    public ResponseEntity<List<PlaylistDto>> getUserPlaylists(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String provider) {
        log.info("Get user playlists: userId={}, provider={}", userId, provider);
        return ResponseEntity.ok(providerService.getUserPlaylists(userId, provider));
    }

}