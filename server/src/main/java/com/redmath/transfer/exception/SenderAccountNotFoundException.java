package com.redmath.transfer.exception;

public class SenderAccountNotFoundException extends RuntimeException {
    public SenderAccountNotFoundException(String message) {
        super(message);
    }
}
