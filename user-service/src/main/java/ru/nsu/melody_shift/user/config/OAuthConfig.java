package ru.nsu.melody_shift.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.user.service.oauth.OAuthService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Configuration
public class OAuthConfig {

    @Bean
    public Map<MusicPlatform, OAuthService> oauthServiceMap(List<OAuthService> oauthServices) {
        return oauthServices.stream()
                .collect(Collectors.toMap(
                        OAuthService::getPlatform,
                        Function.identity()
                ));
    }
}
