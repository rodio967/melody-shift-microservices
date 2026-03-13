package ru.nsu.melody_shift.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private String tokenType = "Bearer";

    private Long userId;

    private String username;

    private String email;

    private Set<String> roles;

    public AuthResponse(String token, Long userId, String username, String email, Set<String> roles) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
