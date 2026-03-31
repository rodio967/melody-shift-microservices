package ru.nsu.melody_shift.user.service.oauth;

import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.user.domain.OAuthToken;
import ru.nsu.melody_shift.user.domain.User;

public interface OAuthService {

    String getAuthorizationUrl(String state);

    OAuthToken exchangeCodeForToken(String code, User user);

    OAuthToken refreshAndSave(User user);

    MusicPlatform getPlatform();
}
