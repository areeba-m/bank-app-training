package com.redmath.exception_utility.exception;

public abstract class ResourceNotFoundException extends RuntimeException {
    protected ResourceNotFoundException(String message) {
        super(message);
    }
}