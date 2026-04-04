package ru.nsu.melody_shift.user.service.oauth;

import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.user.dto.OAuthTokenResponse;
import ru.nsu.melody_shift.user.dto.PlatformUserInfo;

public interface OAuthService {

    String getAuthorizationUrl(String state);

    OAuthTokenResponse exchangeCode(String code);

    PlatformUserInfo fetchUserInfo(String accessToken);

    OAuthTokenResponse refresh(String plainRefreshToken);

    Long getDefaultTokenLife();

    MusicPlatform getPlatform();
}
