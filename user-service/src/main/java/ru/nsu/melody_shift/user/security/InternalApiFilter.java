package ru.nsu.melody_shift.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalApiFilter extends OncePerRequestFilter {

    private static final String INTERNAL_PATH_PREFIX = "/api/internal/";
    private static final String HEADER_NAME = "X-Internal-Secret";

    private final String internalSecret;

    public InternalApiFilter(
            @Value("${internal.api.secret}") String internalSecret
    ) {
        this.internalSecret = internalSecret;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX)) {
            String provided = request.getHeader(HEADER_NAME);

            if (provided == null || !MessageDigest.isEqual(provided.getBytes(StandardCharsets.UTF_8), internalSecret.getBytes(StandardCharsets.UTF_8))) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Access denied to internal API\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}