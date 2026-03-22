package ru.nsu.melody_shift.user.service.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.nsu.melody_shift.user.dto.OAuthTokenResponse;
import ru.nsu.melody_shift.user.dto.PlatformUserInfo;
import ru.nsu.melody_shift.user.domain.OAuthToken;
import ru.nsu.melody_shift.user.domain.User;
import ru.nsu.melody_shift.user.service.OAuthTokenService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor
public abstract class AbstractOAuthService implements OAuthService {

    protected final OAuthTokenService oauthTokenService;

    protected final String clientId;
    protected final String clientSecret;
    protected final String redirectUri;



    @Override
    public OAuthToken exchangeCodeForToken(String code, User user) {
        // 1) Обменять code на access/refresh tokens
        OAuthTokenResponse tokenResponse = fetchToken(code);

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new RuntimeException("Failed to get token from " + getPlatform());
        }

        // 2) Получить информацию о пользователе платформы
        PlatformUserInfo userInfo = fetchUserInfo(tokenResponse.getAccessToken());

        if (userInfo == null || userInfo.getId() == null) {
            throw new RuntimeException("Failed to get user info from " + getPlatform());
        }

        // 3) Сохранить токен в БД
        Long expiresIn = tokenResponse.getExpiresIn() != null
                ? tokenResponse.getExpiresIn()
                : getDefaultTokenLife();

        return oauthTokenService.saveOrUpdateToken(
                user,
                getPlatform(),
                userInfo.getId(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                expiresIn,
                tokenResponse.getScope()
        );
    }

    protected abstract OAuthTokenResponse fetchToken(String code);

    protected abstract PlatformUserInfo fetchUserInfo(String accessToken);

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
