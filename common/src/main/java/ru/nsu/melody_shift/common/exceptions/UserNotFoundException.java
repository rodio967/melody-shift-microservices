package ru.nsu.melody_shift.common.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("Пользователь с id " + userId + " не найден");
    }

    public UserNotFoundException(String username) {
        super("Пользователь с username " + username + " не найден");
    }
}