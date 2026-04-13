package ru.nsu.melody_shift.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.nsu.melody_shift.gateway.security.JwtClaims;
import ru.nsu.melody_shift.gateway.security.JwtValidator;
import ru.nsu.melody_shift.gateway.util.ErrorResponseWriter;


@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtValidator jwtValidator;
    private final ErrorResponseWriter errorResponseWriter;

    public JwtAuthenticationFilter(JwtValidator jwtValidator, ErrorResponseWriter errorResponseWriter) {
        super(Config.class);
        this.jwtValidator = jwtValidator;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return errorResponseWriter.writeError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                JwtClaims claims = jwtValidator.validateAndExtractClaims(token);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Name", claims.username())
                        .header("X-User-Id", String.valueOf(claims.userId()))
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                log.warn("JWT validation failed: {}", e.getMessage());
                return errorResponseWriter.writeError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            }
        };
    }

    public static class Config {
    }
}
