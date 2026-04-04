package ru.nsu.melody_shift.user.service.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.nsu.melody_shift.user.dto.OAuthTokenResponse;
import ru.nsu.melody_shift.user.dto.PlatformUserInfo;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOAuthService implements OAuthService {

    protected final String clientId;
    protected final String clientSecret;
    protected final String redirectUri;


    @Override
    public OAuthTokenResponse exchangeCode(String code) {
        log.info("[{}] Exchanging code for token", getPlatform());

        OAuthTokenResponse tokenResponse = fetchToken(code);

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            log.error("[{}] Failed to get token — empty response", getPlatform());
            throw new RuntimeException("Failed to get token from " + getPlatform());
        }

        log.info("[{}] Token received successfully", getPlatform());

        return tokenResponse;
    }

    @Override
    public PlatformUserInfo fetchUserInfo(String accessToken) {
        log.info("[{}] Fetching user info", getPlatform());

        PlatformUserInfo userInfo = getUserInfo(accessToken);

        if (userInfo == null || userInfo.getId() == null) {
            log.error("[{}] Failed to get user info — empty response", getPlatform());
            throw new RuntimeException("Failed to get user info from " + getPlatform());
        }

        return userInfo;
    }

    @Override
    public OAuthTokenResponse refresh(String plainRefreshToken) {
        log.info("[{}] Refreshing access token", getPlatform());

        OAuthTokenResponse tokenResponse = refreshAccessToken(plainRefreshToken);

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            log.error("[{}] Failed to refresh — empty response", getPlatform());
            throw new RuntimeException("Failed to refresh token from " + getPlatform());
        }

        log.info("[{}] Token refresh successfully", getPlatform());

        return tokenResponse;
    }

    protected abstract OAuthTokenResponse fetchToken(String code);

    protected abstract PlatformUserInfo getUserInfo(String accessToken);

    protected abstract OAuthTokenResponse refreshAccessToken(String refreshToken);

    public Long getDefaultTokenLife() {
        return 3600L;
    }

    protected String encodeCredentials() {
        String auth = clientId + ":" + clientSecret;
        return Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    protected MultiValueMap<String, String> buildTokenRequestBody(String code) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", code);
        requestBody.add("redirect_uri", redirectUri);

        return requestBody;
    }


}
