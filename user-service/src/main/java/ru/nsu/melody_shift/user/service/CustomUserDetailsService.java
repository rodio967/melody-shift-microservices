package ru.nsu.melody_shift.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.nsu.melody_shift.user.domain.User;
import ru.nsu.melody_shift.user.repository.UserRepository;
import ru.nsu.melody_shift.user.security.CustomUserDetails;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь с логином (" + username + ") не найден"
                ));

        return CustomUserDetails.build(user);
    }
}
