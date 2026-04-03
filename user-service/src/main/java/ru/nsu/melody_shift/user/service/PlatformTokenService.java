package ru.nsu.melody_shift.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.user.domain.OAuthToken;
import ru.nsu.melody_shift.user.domain.User;
import ru.nsu.melody_shift.user.dto.OAuthTokenResponse;
import ru.nsu.melody_shift.user.dto.PlatformUserInfo;
import ru.nsu.melody_shift.user.service.oauth.OAuthService;
import ru.nsu.melody_shift.user.service.oauth.exceptions.PlatformNotConnectedException;
import ru.nsu.melody_shift.user.service.oauth.exceptions.TokenExpiredException;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformTokenService {

    private final OAuthTokenService tokenService;
    private final Map<MusicPlatform, OAuthService> oauthServices;

    public OAuthToken connectPlatform(String code, User user, MusicPlatform platform) {
        log.info("[{}] Connecting platform to user {}", platform, user.getUsername());

        OAuthService oauthService = resolveService(platform);

        OAuthTokenResponse tokenResponse = oauthService.exchangeCode(code);

        PlatformUserInfo userInfo = oauthService.fetchUserInfo(tokenResponse.getAccessToken());

        Long expiresIn = tokenResponse.getExpiresIn() != null
                ? tokenResponse.getExpiresIn()
                : oauthService.getDefaultTokenLife();

        log.info("[{}] Platform connected to user {}", platform, user.getUsername());

        return tokenService.saveToken(user,
                platform,
                userInfo.getId(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                expiresIn,
                tokenResponse.getScope());
    }

    public OAuthToken getValidToken(User user, MusicPlatform platform) {
        OAuthToken token = tokenService.getToken(user, platform)
                .orElseThrow(() -> new PlatformNotConnectedException(platform));

        if (!token.isExpired()) {
            return token;
        }

        log.info("[{}] Token expired for user={}, refreshing", platform, user.getUsername());
        return refreshToken(token, platform);

    }

    private OAuthToken refreshToken(OAuthToken token, MusicPlatform platform) {
        OAuthService oauthService = resolveService(platform);

        String plainRefreshToken = tokenService.getDecryptedRefreshToken(token);

        try {
            OAuthTokenResponse tokenResponse = oauthService.refresh(plainRefreshToken);

            String newRefreshToken = tokenResponse.getRefreshToken() != null
                    ? tokenResponse.getRefreshToken()
                    : plainRefreshToken;

            Long expiresIn = tokenResponse.getExpiresIn() != null
                    ? tokenResponse.getExpiresIn()
                    : oauthService.getDefaultTokenLife();

            return tokenService.updateToken(
                    token,
                    tokenResponse.getAccessToken(),
                    newRefreshToken,
                    expiresIn
            );
        } catch (Exception e) {
            log.error("[{}] Refresh failed for user={}, disconnecting platform", platform, token.getUser().getUsername());
            tokenService.deleteToken(token.getUser(), platform);
            throw new TokenExpiredException(platform);
        }
    }

    private OAuthService resolveService(MusicPlatform platform) {
        OAuthService oauthService = oauthServices.get(platform);

        if (oauthService == null) {
            throw new RuntimeException("OAuth service not found for: " + platform);
        }

        return oauthService;
    }

}
