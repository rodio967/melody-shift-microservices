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
import ru.nsu.melody_shift.user.service.OAuthTokenService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class YandexOAuthService extends AbstractOAuthService {

    private final RestClient tokenClient;
    private final RestClient apiClient;

    private static final String AUTHORIZE_URL = "https://oauth.yandex.ru/authorize";
    private static final String TOKEN_URL = "https://oauth.yandex.ru/token";
    private static final String API_BASE_URL = "https://login.yandex.ru";
    private static final String SCOPE = "login:email login:info login:avatar";

    public YandexOAuthService(OAuthTokenService oauthTokenService,
                              RestClient.Builder restClientBuilder,
                              @Value("${yandex.client-id}") String clientId,
                              @Value("${yandex.client-secret}") String clientSecret,
                              @Value("${yandex.redirect-uri}") String redirectUri) {
        super(oauthTokenService, clientId, clientSecret, redirectUri);

        this.tokenClient = restClientBuilder
                .baseUrl(TOKEN_URL)
                .build();

        this.apiClient = restClientBuilder
                .baseUrl(API_BASE_URL)
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
                "&force_confirm=yes";
    }

    @Override
    protected OAuthTokenResponse fetchToken(String code) {
        return tokenClient.post()
                .uri("")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodeCredentials())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(buildTokenRequestBody(code))
                .retrieve()
                .body(OAuthTokenResponse.class);
    }

    @Override
    protected PlatformUserInfo fetchUserInfo(String accessToken) {
        return apiClient.get()
                .uri("/info?format=json")
                .header(HttpHeaders.AUTHORIZATION, "OAuth " + accessToken)
                .retrieve()
                .body(PlatformUserInfo.class);
    }

    @Override
    protected OAuthTokenResponse refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "refresh_token");
        requestBody.add("refresh_token", refreshToken);

        OAuthTokenResponse response = tokenClient.post()
                .uri("")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodeCredentials())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody)
                .retrieve()
                .body(OAuthTokenResponse.class);

        return response;
    }

    @Override
    public MusicPlatform getPlatform() {
        return MusicPlatform.YANDEX;
    }
}
