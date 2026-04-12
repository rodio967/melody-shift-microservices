package ru.nsu.melody_shift.common.enums;


import ru.nsu.melody_shift.common.exceptions.UnknownPlatformException;

public enum MusicPlatform {
    SPOTIFY,
    YANDEX,
    VK,
    APPLE_MUSIC;
    
    public String getDisplayName() {
        return switch (this) {
            case SPOTIFY -> "Spotify";
            case YANDEX -> "Yandex Music";
            case VK -> "VK Music";
            case APPLE_MUSIC -> "Apple Music";
        };
    }

    public static MusicPlatform fromString(String value) {
        try {
            return MusicPlatform.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnknownPlatformException("Unknown platform: " + value);
        }
    }
}
