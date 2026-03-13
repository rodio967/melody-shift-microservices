package ru.nsu.melody_shift.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.melody_shift.common.dto.OAuthTokenDto;
import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.user.domain.OAuthToken;
import ru.nsu.melody_shift.user.domain.User;
import ru.nsu.melody_shift.user.repository.OAuthTokenRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OAuthTokenService {

    private final OAuthTokenRepository tokenRepository;

    @Transactional
    public OAuthToken saveOrUpdateToken(
            User user,
            MusicPlatform platform,
            String platformUserId,
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String scope
    ) {
        OAuthToken token = tokenRepository.findByUserAndPlatform(user, platform)
                .orElse(new OAuthToken());

        token.setUser(user);
        token.setPlatform(platform);
        token.setPlatformUserId(platformUserId);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setExpiresAt(Instant.now().plusSeconds(expiresIn));
        token.setScope(scope);

        return tokenRepository.save(token);
    }

    public Optional<OAuthToken> getToken(User user, MusicPlatform platform) {
        return tokenRepository.findByUserAndPlatform(user, platform);
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
                token.getAccessToken(),
                token.getRefreshToken(),
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
