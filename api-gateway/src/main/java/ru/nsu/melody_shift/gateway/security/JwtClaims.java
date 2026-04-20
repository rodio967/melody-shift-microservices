package ru.nsu.melody_shift.gateway.security;

public record JwtClaims(String username, Long userId) {}
