package ru.nsu.melody_shift.gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtValidator {

    private final SecretKey secretKey;

    public JwtValidator(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public JwtClaims validateAndExtractClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new JwtClaims(claims.getSubject(), claims.get("userId", Long.class));
        } catch (ExpiredJwtException ex) {
            log.debug("Expired JWT token");
            throw ex;
        } catch (MalformedJwtException | SecurityException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            throw ex;
        }
    }

}
