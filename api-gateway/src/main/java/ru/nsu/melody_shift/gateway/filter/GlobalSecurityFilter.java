package ru.nsu.melody_shift.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.nsu.melody_shift.gateway.util.ErrorResponseWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalSecurityFilter implements GlobalFilter, Ordered {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/api/internal/")) {
            log.warn("Blocked external access to internal API: {}", path);
            return errorResponseWriter.writeError(exchange, HttpStatus.FORBIDDEN, "Internal API is not accessible");
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
