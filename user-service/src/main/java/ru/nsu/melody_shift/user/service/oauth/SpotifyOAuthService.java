package ru.nsu.melody_shift.user.service.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import ru.nsu.melody_shift.user.dto.OAuthTokenResponse;
import ru.nsu.melody_shift.user.dto.PlatformUserInfo;
import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.user.service.OAuthTokenService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SpotifyOAuthService extends AbstractOAuthService {

    private final RestClient tokenClient;
    private final RestClient apiClient;

    private static final String AUTHORIZE_URL = "https://accounts.spotify.com/authorize";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String API_BASE_URL = "https://api.spotify.com/v1";
    private static final String SCOPE = "user-read-email playlist-read-private playlist-read-collaborative playlist-modify-private playlist-modify-public";

    public SpotifyOAuthService(
            OAuthTokenService oauthTokenService,
            RestClient.Builder restClientBuilder,
            @Value("${spotify.client-id}") String clientId,
            @Value("${spotify.client-secret}") String clientSecret,
            @Value("${spotify.redirect-uri}") String redirectUri
    ) {
        super(oauthTokenService, clientId, clientSecret, redirectUri);

        this.tokenClient = restClientBuilder.clone()
                .baseUrl(TOKEN_URL)
                .build();

        this.apiClient = restClientBuilder.clone()
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
                "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
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
                .uri("/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
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
        return MusicPlatform.SPOTIFY;
    }
}
