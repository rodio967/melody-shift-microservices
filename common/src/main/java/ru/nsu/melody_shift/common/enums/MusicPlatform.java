package ru.nsu.melody_shift.common.enums;


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
}
