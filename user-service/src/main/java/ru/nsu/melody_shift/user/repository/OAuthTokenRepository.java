package ru.nsu.melody_shift.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.melody_shift.common.enums.MusicPlatform;
import ru.nsu.melody_shift.user.domain.OAuthToken;
import ru.nsu.melody_shift.user.domain.User;

import java.util.List;
import java.util.Optional;


@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {

    Optional<OAuthToken> findByUserAndPlatform(User user, MusicPlatform platform);

    List<OAuthToken> findByUser(User user);

    void deleteByUserAndPlatform(User user, MusicPlatform platform);

    boolean existsByUserAndPlatform(User user, MusicPlatform platform);
}
