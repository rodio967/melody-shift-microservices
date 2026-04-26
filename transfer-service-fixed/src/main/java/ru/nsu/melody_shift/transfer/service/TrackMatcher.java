package ru.nsu.melody_shift.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nsu.melody_shift.common.dto.TrackDto;
import ru.nsu.melody_shift.transfer.client.ProviderClient;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Отвечает за поиск соответствия трека на целевой платформе.
 * Использует fuzzy matching по названию и артисту.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackMatcher {

    private final ProviderClient providerClient;

    private static final double TITLE_THRESHOLD = 0.7;

    /**
     * Ищет трек на целевой платформе по названию и артисту.
     *
     * @return platformId найденного трека или empty
     */
    public Optional<String> findMatch(String targetProvider, TrackDto sourceTrack, String userId) {
        String query = sourceTrack.getTitle();
        String artist = sourceTrack.getArtist();

        List<TrackDto> candidates = providerClient.searchTracks(
                targetProvider, query, artist, userId);

        if (candidates == null || candidates.isEmpty()) {
            log.debug("No search results for '{}' by '{}'", query, artist);
            return Optional.empty();
        }

        return candidates.stream()
                .filter(candidate -> isMatch(candidate, sourceTrack))
                .findFirst()
                .map(TrackDto::getPlatformId);
    }

    private boolean isMatch(TrackDto candidate, TrackDto source) {
        boolean artistMatch = isArtistMatch(candidate.getArtist(), source.getArtist());
        double titleSimilarity = calculateSimilarity(
                normalize(candidate.getTitle()),
                normalize(source.getTitle())
        );

        return artistMatch && titleSimilarity >= TITLE_THRESHOLD;
    }

    /**
     * Проверяет совпадение артистов с учётом:
     * - точного совпадения после нормализации
     * - вхождения одного в другого ("Beatles" vs "The Beatles")
     * - перестановки слов ("Lennon John" vs "John Lennon")
     */
    private boolean isArtistMatch(String a, String b) {
        if (a == null || b == null) return false;

        String na = normalize(a);
        String nb = normalize(b);

        if (na.equals(nb)) return true;
        if (na.contains(nb) || nb.contains(na)) return true;

        // Проверка перестановки слов
        var wordsA = Set.of(na.split("\\s+"));
        var wordsB = Set.of(nb.split("\\s+"));
        if (!wordsA.isEmpty() && wordsA.equals(wordsB)) return true;

        return false;
    }

    /**
     * Нормализация строки: lowercase, удаление скобок и feat./ft.
     */
    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase()
                .replaceAll("\\(.*?\\)", "")     // убираем скобки (remastered 2020)
                .replaceAll("\\[.*?]", "")        // убираем квадратные скобки
                .replaceAll("feat\\.?|ft\\.?", "") // убираем feat./ft.
                .replaceAll("[^a-zа-яё0-9\\s]", "") // только буквы, цифры, пробелы
                .replaceAll("\\s+", " ")           // нормализуем пробелы
                .trim();
    }

    /**
     * Рассчитывает схожесть двух строк (0.0 — полностью разные, 1.0 — идентичные).
     * Использует нормализованное расстояние Левенштейна.
     */
    private double calculateSimilarity(String a, String b) {
        if (a.equals(b)) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        // Быстрая проверка вхождения
        if (a.contains(b) || b.contains(a)) return 0.9;

        int maxLen = Math.max(a.length(), b.length());
        int distance = levenshteinDistance(a, b);
        return 1.0 - ((double) distance / maxLen);
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }
}
