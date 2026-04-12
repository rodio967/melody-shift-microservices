package ru.nsu.melody_shift.user.store;

public interface OAuthStateStore {
    void save(String state, Long userId);

    Long getAndRemove(String state);
}
