package com.redmath.authentication.exception;

import lombok.Getter;

@Getter
public class OtpCooldownException extends RuntimeException {
    private final long secondsRemaining;
    public OtpCooldownException(String message, long secondsRemaining) {
        super(message);
        this.secondsRemaining = secondsRemaining;
    }
}
