package ru.nsu.melody_shift.user.controller;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.user.controller.exception.OAuthRedirectException;
import ru.nsu.melody_shift.user.domain.User;
import ru.nsu.melody_shift.common.exceptions.UnknownPlatformException;
import ru.nsu.melody_shift.user.service.UserService;
import ru.nsu.melody_shift.user.service.oauth.OAuthService;
import ru.nsu.melody_shift.user.service.PlatformTokenService;
import ru.nsu.melody_shift.user.store.OAuthStateStore;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final Map<MusicPlatform, OAuthService> oauthServices;
    private final PlatformTokenService platformTokenService;
    private final UserService userService;
    private final OAuthStateStore oAuthStateStore;

    @ExceptionHandler(OAuthRedirectException.class)
    public RedirectView handleOAuthRedirect(OAuthRedirectException ex) {
        log.error(ex.getMessage());
        return new RedirectView(ex.getRedirectUrl());
    }

    @ExceptionHandler(UnknownPlatformException.class)
    public RedirectView handleUnknowPlatform(UnknownPlatformException ex) {
        log.error(ex.getMessage());
        return new RedirectView("/dashboard?error=unknown_platform");
    }

    @GetMapping("/{platform}/authorize")
    public RedirectView authorize(
            @PathVariable String platform,
            Authentication auth
    ) {
        log.info("[{}] Starting OAuth flow", platform);

        if (auth == null || !auth.isAuthenticated()) {
            throw new OAuthRedirectException("/login?error=not_authenticated",
                    "Unauthorized access to OAuth authorize");
        }

        OAuthService oauthService = resolveService(platform);

        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new OAuthRedirectException("/login?error=user_not_found",
                        "User not found: " + auth.getName()));

        String state = UUID.randomUUID().toString();
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
        log.info("[{}] Received OAuth callback", platform);

        if (error != null) {
            throw new OAuthRedirectException("/dashboard?error=" + platform + "_declined",
                    "User declined OAuth for platform " + platform + ": " + error);
        }

        if (code == null || code.isEmpty()) {
            throw new OAuthRedirectException("/dashboard?error=no_code",
                    "OAuth callback without code");
        }

        if (state == null || state.isEmpty()) {
            throw new OAuthRedirectException("/dashboard?error=no_state",
                    "OAuth callback without state");
        }

        OAuthService oauthService = resolveService(platform);

        Long userId;
        try {
            userId = oAuthStateStore.getAndRemove(state);
        } catch (Exception e) {
            throw new OAuthRedirectException("/dashboard?error=invalid_or_expired_state",
                    "Invalid or expired state: " + state);
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new OAuthRedirectException("/login?error=user_not_found",
                        "User not found in callback: " + userId));

        try {
            platformTokenService.connectPlatform(code, user, oauthService.getPlatform());
            log.info("[{}] Successfully connected for user {}", platform , user.getUsername());
        } catch (Exception e) {
            throw new OAuthRedirectException("/dashboard?error=" + platform + "_failed",
                    platform + " OAuth error: " + e.getMessage());
        }

        return new RedirectView("/dashboard?success=" + platform + "_connected");
    }


    private OAuthService resolveService(String platform) {
        MusicPlatform musicPlatform = MusicPlatform.fromString(platform);
        OAuthService service = oauthServices.get(musicPlatform);

        if (service == null) {
            throw new OAuthRedirectException("/dashboard?error=service_not_found",
                    "OAuth service not found for platform: " + platform);
        }

        return service;
    }


}
