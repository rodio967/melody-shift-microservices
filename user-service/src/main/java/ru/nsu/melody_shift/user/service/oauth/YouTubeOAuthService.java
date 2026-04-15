package ru.nsu.melody_shift.user.service.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.user.dto.OAuthTokenResponse;
import ru.nsu.melody_shift.user.dto.PlatformUserInfo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class YouTubeOAuthService extends AbstractOAuthService {

    private final RestClient tokenClient;
    private final RestClient userInfoClient;

    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3";
    private static final String SCOPE = "https://www.googleapis.com/auth/youtube " +
            "https://www.googleapis.com/auth/userinfo.email " +
            "https://www.googleapis.com/auth/userinfo.profile";

    public YouTubeOAuthService(
            RestClient.Builder restClientBuilder,
            @Value("${youtube.client-id}") String clientId,
            @Value("${youtube.client-secret}") String clientSecret,
            @Value("${youtube.redirect-uri}") String redirectUri
    ) {
        super(clientId, clientSecret, redirectUri);

        this.tokenClient = restClientBuilder.clone()
                .baseUrl(TOKEN_URL)
                .build();

        this.userInfoClient = restClientBuilder.clone()
                .baseUrl(USERINFO_URL)
                .build();
    }

    @Override
    public String getAuthorizationUrl(String state) {
        return AUTHORIZE_URL + "?" +
                "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8) +
                "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8) +
                "&access_type=offline" +
                "&prompt=consent";
    }

    @Override
    protected OAuthTokenResponse fetchToken(String code) {
        MultiValueMap<String, String> body = buildTokenRequestBody(code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        return tokenClient.post()
                .uri("")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(OAuthTokenResponse.class);
    }

    @Override
    protected PlatformUserInfo getUserInfo(String accessToken) {
        return userInfoClient.get()
                .uri("/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(PlatformUserInfo.class);
    }

    @Override
    protected OAuthTokenResponse refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        OAuthTokenResponse response = tokenClient.post()
                .uri("")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(OAuthTokenResponse.class);

        if (response != null && response.getRefreshToken() == null) {
            log.debug("[{}] No new refresh token (expected for Google)", getPlatform());
        }

        return response;
    }

    @Override
    public MusicPlatform getPlatform() {
        return MusicPlatform.YOUTUBE;
    }
}