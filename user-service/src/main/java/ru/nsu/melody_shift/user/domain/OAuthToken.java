package ru.nsu.melody_shift.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nsu.melody_shift.common.enums.MusicPlatform;

import java.time.Instant;


@Entity
@Table(name = "oauth_tokens", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "platform"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MusicPlatform platform;


    @Column(nullable = false, length = 100)
    private String platformUserId;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String accessToken;


    @Column(columnDefinition = "TEXT")
    private String refreshToken;


    @Column(nullable = false)
    private Instant expiresAt;


    @Column(length = 500)
    private String scope;


    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }


    public boolean isExpired() {
        return Instant.now().plusSeconds(60).isAfter(expiresAt);
    }
}
