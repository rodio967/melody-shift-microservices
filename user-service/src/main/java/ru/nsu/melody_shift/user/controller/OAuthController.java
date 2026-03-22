package ru.nsu.melody_shift.user.controller;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.user.domain.User;
import ru.nsu.melody_shift.common.exceptions.UnknownPlatformException;
import ru.nsu.melody_shift.user.service.OAuthTokenService;
import ru.nsu.melody_shift.user.service.UserService;
import ru.nsu.melody_shift.user.service.oauth.OAuthService;
import ru.nsu.melody_shift.user.store.OAuthStateStore;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final Map<MusicPlatform, OAuthService> oauthServices;
    private final OAuthTokenService oauthTokenService;
    private final UserService userService;
    private final OAuthStateStore oAuthStateStore;

    @GetMapping("/{platform}/authorize")
    public RedirectView authorize(
            @PathVariable String platform,
            Authentication auth
    ) {
        log.info("Starting OAuth flow for platform: {}", platform);

        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthorized access to OAuth authorize");
            return new RedirectView("/login?error=not_authenticated");
        }

        MusicPlatform musicPlatform;
        try {
            musicPlatform = MusicPlatform.fromString(platform);
        } catch (UnknownPlatformException e) {
            log.error("Unknown platform: {}", platform);
            return new RedirectView("/dashboard?error=unknown_platform");
        }

        OAuthService oauthService = oauthServices.get(musicPlatform);
        if (oauthService == null) {
            log.error("OAuth service not found for platform: {}", platform);
            return new RedirectView("/dashboard?error=service_not_found");
        }

        String state = UUID.randomUUID().toString();
        User user = userService.findByUsername(auth.getName())
                .orElse(null);

        if (user == null) {
            log.error("User not found: {}", auth.getName());
            return new RedirectView("/login?error=user_not_found");
        }

        oAuthStateStore.save(state, user.getId());

        String authUrl = oauthService.getAuthorizationUrl(state);
        log.info("Redirecting to OAuth provider: {}", authUrl);

        return new RedirectView(authUrl);
    }


    @GetMapping("/{platform}/callback")
    public RedirectView callback(
            @PathVariable String platform,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error
    ) {
        log.info("Received OAuth callback for platform: {}", platform);

        if (error != null) {
            log.warn("User declined OAuth for platform {}: {}", platform, error);
            return new RedirectView("/dashboard?error=" + platform + "_declined");
        }

        if (code == null || code.isEmpty()) {
            log.error("OAuth callback without code");
            return new RedirectView("/dashboard?error=no_code");
        }

        if (state == null || state.isEmpty()) {
            log.error("OAuth callback without state");
            return new RedirectView("/dashboard?error=no_state");
        }

        MusicPlatform musicPlatform;
        try {
            musicPlatform = MusicPlatform.fromString(platform);
        } catch (UnknownPlatformException e) {
            log.error("Unknown platform in callback: {}", platform);
            return new RedirectView("/dashboard?error=unknown_platform");
        }

        OAuthService oauthService = oauthServices.get(musicPlatform);

        if (oauthService == null) {
            log.error("OAuth service not found in callback for platform: {}", platform);
            return new RedirectView("/dashboard?error=service_not_found");
        }

        Long userId;
        try {
            userId = oAuthStateStore.getAndRemove(state);
        } catch (Exception e) {
            return new RedirectView("/dashboard?error=invalid_or_expired_state");
        }

        User user = userService.findById(userId)
                .orElse(null);

        if (user == null) {
            log.error("User not found in callback: {}", userId);
            return new RedirectView("/login?error=user_not_found");
        }

        try {
            oauthService.exchangeCodeForToken(code, user);
            log.info("Successfully connected {} for user {}", platform, user.getUsername());
        } catch (Exception e) {
            log.error("{} OAuth error: {}", platform, e.getMessage());
            return new RedirectView("/dashboard?error=" + platform + "_failed");
        }

        return new RedirectView("/dashboard?success=" + platform + "_connected");
    }


}
