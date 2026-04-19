package ru.nsu.melody_shift.providerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "spotify-api", url = "https://api.spotify.com/v1")
public interface SpotifyApiFeignClient {

    @GetMapping("/search")
    String searchTracks(@RequestParam("q") String query,
                        @RequestParam("type") String type,
                        @RequestParam("limit") int limit,
                        @RequestHeader("Authorization") String authorization);

    @GetMapping("/playlists/{playlistId}/tracks")
    String getPlaylistTracks(@PathVariable("playlistId") String playlistId,
                             @RequestHeader("Authorization") String authorization);

    @PostMapping("/users/{userId}/playlists")
    String createPlaylist(@PathVariable("userId") String userId,
                          @RequestBody String requestBody,
                          @RequestHeader("Authorization") String authorization);

    @PostMapping("/playlists/{playlistId}/tracks")
    String addTrackToPlaylist(@PathVariable("playlistId") String playlistId,
                              @RequestBody String requestBody,
                              @RequestHeader("Authorization") String authorization);

    @GetMapping("/me/playlists")
    String getUserPlaylists(@RequestHeader("Authorization") String authorization);
}