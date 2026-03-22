package ru.nsu.melody_shift.user.store;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryOAuthStateStore implements OAuthStateStore {

    private final ConcurrentHashMap<String, StateEntry> store = new ConcurrentHashMap<>();

    @Override
    public void save(String state, Long userId) {
        store.put(state, new StateEntry(userId, Instant.now().plusSeconds(600)));
    }

    @Override
    public Long getAndRemove(String state) {
        StateEntry entry = store.remove(state);

        if (entry == null) {
            throw new RuntimeException("Invalid state");
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            throw new RuntimeException("State expired");
        }

        return entry.userId();
    }

    @Scheduled(fixedDelay = 300_000)
    public void cleanup() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
    }

    private record StateEntry(Long userId, Instant expiresAt) {}
}
