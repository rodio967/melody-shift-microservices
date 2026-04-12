package ru.nsu.melody_shift.user.service.oauth.exceptions;

import ru.nsu.melody_shift.common.enums.MusicPlatform;

public class TokenExpiredException extends RuntimeException {
  private final MusicPlatform platform;

  public TokenExpiredException(MusicPlatform platform) {
    super("Сессия " + platform.getDisplayName() + " истекла");
    this.platform = platform;
  }

  public MusicPlatform getPlatform() {
    return platform;
  }
}
