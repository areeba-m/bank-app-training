package com.redmath.authentication.exception;

public class InvalidTokenScopeException extends RuntimeException {
    public InvalidTokenScopeException(String message) {
        super(message);
    }
}
