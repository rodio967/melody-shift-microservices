package ru.nsu.melody_shift.user.controller.exception;

public class OAuthRedirectException extends RuntimeException {
    private final String redirectUrl;

    public OAuthRedirectException(String redirectUrl, String message) {
        super(message);

        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
