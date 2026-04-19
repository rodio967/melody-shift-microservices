package ru.nsu.melody_shift.TransferService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.nsu.melody_shift.common.dto.PlaylistDto;
import ru.nsu.melody_shift.common.dto.TrackDto;
import java.util.List;

@FeignClient(name = "provider-service", url = "${provider.service.url:http://localhost:8082}")
public interface ProviderClient {

    @GetMapping("/api/providers/{provider}/search")
    List<TrackDto> searchTracks(@PathVariable("provider") String provider,
                                @RequestParam("query") String query,
                                @RequestParam("artist") String artist,
                                @RequestHeader("X-User-Id") String userId);

    @GetMapping("/api/providers/{provider}/playlists/{playlistId}/tracks")
    List<TrackDto> getPlaylistTracks(@PathVariable("provider") String provider,
                                     @PathVariable("playlistId") String playlistId,
                                     @RequestHeader("X-User-Id") String userId);

    @PostMapping("/api/providers/{provider}/playlists")
    String createPlaylist(@PathVariable("provider") String provider,
                          @RequestParam("name") String name,
                          @RequestHeader("X-User-Id") String userId);

    @PostMapping("/api/providers/{provider}/playlists/{playlistId}/tracks")
    void addTrack(@PathVariable("provider") String provider,
                  @PathVariable("playlistId") String playlistId,
                  @RequestParam("trackId") String trackId,
                  @RequestHeader("X-User-Id") String userId);

    @GetMapping("/api/providers/{provider}/playlists")
    List<PlaylistDto> getUserPlaylists(@PathVariable("provider") String provider,
                                       @RequestHeader("X-User-Id") String userId);
}