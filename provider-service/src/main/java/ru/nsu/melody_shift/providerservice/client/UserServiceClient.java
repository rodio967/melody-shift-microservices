package ru.nsu.melody_shift.providerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import ru.nsu.melody_shift.common.dto.OAuthTokenDto;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {
    //TODO ("/internal/users/{userId}/tokens") думаю лучше будет
    @GetMapping("/{userId}/tokens")
    OAuthTokenDto getUserToken(@PathVariable("userId") Long userId,
                               @RequestParam("platform") String platform,
                               @RequestHeader("X-Internal-Secret") String secret);
}