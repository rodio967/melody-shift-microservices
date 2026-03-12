package ru.nsu.melody_shift.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthTokenDto {

    private String accessToken;

    private String refreshToken;

    private Instant expiresAt;

    private String platformUserId;
}
