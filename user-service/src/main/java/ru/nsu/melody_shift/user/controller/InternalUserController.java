package ru.nsu.melody_shift.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nsu.melody_shift.common.dto.OAuthTokenDto;
import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.common.exceptions.UserNotFoundException;
import ru.nsu.melody_shift.user.domain.OAuthToken;
import ru.nsu.melody_shift.user.domain.User;
import ru.nsu.melody_shift.user.service.OAuthTokenService;
import ru.nsu.melody_shift.user.service.UserService;
import ru.nsu.melody_shift.user.service.PlatformTokenService;

import java.util.List;
import java.util.Map;

/**
 * Internal API для других микросервисов
 * 
 * Endpoints:
 * - GET /api/internal/users/{userId}/tokens?platform=SPOTIFY - получить токен пользователя
 * - GET /api/internal/users/{userId}/platforms - список подключенных платформ
 */
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;
    private final OAuthTokenService tokenService;
    private final PlatformTokenService platformTokenService;

    /**
     * Получить OAuth токен пользователя для платформы
     *
     * @param userId ID пользователя
     * @param platform платформа
     * @return OAuth токен
     */
    @GetMapping("/{userId}/tokens")
    public ResponseEntity<?> getUserToken(
            @PathVariable Long userId,
            @RequestParam MusicPlatform platform
    ) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        OAuthToken token = platformTokenService.getValidToken(user, platform);
        OAuthTokenDto dto = tokenService.toDto(token);

        return ResponseEntity.ok(dto);
    }

    /**
     * Получить список подключенных платформ пользователя
     * 
     * @param userId ID пользователя
     * @return список платформ
     */
    @GetMapping("/{userId}/platforms")
    public ResponseEntity<?> getConnectedPlatforms(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<MusicPlatform> platforms = tokenService.getConnectedPlatforms(user);

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "platforms", platforms
        ));
    }

    /**
     * Проверить существует ли пользователь
     * 
     * @param userId ID пользователя
     * @return информация о существовании
     */
    @GetMapping("/{userId}/exists")
    public ResponseEntity<?> userExists(@PathVariable Long userId) {
        boolean exists = userService.findById(userId).isPresent();
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
