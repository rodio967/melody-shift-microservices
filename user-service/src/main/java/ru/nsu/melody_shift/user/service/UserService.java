package ru.nsu.melody_shift.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.melody_shift.common.exceptions.EmailAlreadyExistsException;
import ru.nsu.melody_shift.common.exceptions.UsernameAlreadyExistsException;
import ru.nsu.melody_shift.user.domain.User;
import ru.nsu.melody_shift.user.repository.UserRepository;

import java.util.Collections;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerNewUser(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Логин уже занят");
        }

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email уже занят");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRoles(Collections.singleton("ROLE_USER"));
        user.setEnabled(true);

        return userRepository.save(user);
    }


    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
