package ru.nsu.melody_shift.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.melody_shift.common.dto.OAuthTokenDto;
import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.user.domain.OAuthToken;
import ru.nsu.melody_shift.user.domain.User;
import ru.nsu.melody_shift.user.repository.OAuthTokenRepository;
import ru.nsu.melody_shift.user.security.TokenEncryptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OAuthTokenService {

    private final OAuthTokenRepository tokenRepository;
    private final TokenEncryptor encryptor;

    @Transactional
    public OAuthToken saveToken(
            User user,
            MusicPlatform platform,
            String platformUserId,
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String scope
    ) {
        OAuthToken token = new OAuthToken();

        token.setUser(user);
        token.setPlatform(platform);
        token.setPlatformUserId(platformUserId);
        token.setAccessToken(encryptor.encrypt(accessToken));
        token.setRefreshToken(encryptor.encrypt(refreshToken));
        token.setExpiresAt(Instant.now().plusSeconds(expiresIn));
        token.setScope(scope);

        return tokenRepository.save(token);
    }

    @Transactional
    public OAuthToken updateToken(
            OAuthToken token,
            String newAccessToken,
            String newRefreshToken,
            Long expiresIn) {
        token.setAccessToken(encryptor.encrypt(newAccessToken));
        token.setRefreshToken(encryptor.encrypt(newRefreshToken));
        token.setExpiresAt(Instant.now().plusSeconds(expiresIn));

        return tokenRepository.save(token);
    }

    public Optional<OAuthToken> getToken(User user, MusicPlatform platform) {
        return tokenRepository.findByUserAndPlatform(user, platform);
    }

    public String getDecryptedRefreshToken(OAuthToken token) {
        return encryptor.decrypt(token.getRefreshToken());
    }

    public List<OAuthToken> getUserTokens(User user) {
        return tokenRepository.findByUser(user);
    }


    @Transactional
    public void deleteToken(User user, MusicPlatform platform) {
        tokenRepository.deleteByUserAndPlatform(user, platform);
    }


    public boolean hasToken(User user, MusicPlatform platform) {
        return tokenRepository.existsByUserAndPlatform(user, platform);
    }


    public OAuthTokenDto toDto(OAuthToken token) {
        return new OAuthTokenDto(
                encryptor.decrypt(token.getAccessToken()),
                encryptor.decrypt(token.getRefreshToken()),
                token.getExpiresAt(),
                token.getPlatformUserId()
        );
    }


    public List<MusicPlatform> getConnectedPlatforms(User user) {
        return tokenRepository.findByUser(user).stream()
                .map(OAuthToken::getPlatform)
                .collect(Collectors.toList());
    }
}
