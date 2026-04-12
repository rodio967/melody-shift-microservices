package ru.nsu.melody_shift.user.service.oauth.exceptions;

import ru.nsu.melody_shift.common.enums.MusicPlatform;

public class PlatformNotConnectedException extends RuntimeException {
    private final MusicPlatform platform;

    public PlatformNotConnectedException(MusicPlatform platform) {
        super("Платформа " + platform.getDisplayName() + " не подключена");
        this.platform = platform;
    }

    public MusicPlatform getPlatform() {
        return platform;
    }
}
