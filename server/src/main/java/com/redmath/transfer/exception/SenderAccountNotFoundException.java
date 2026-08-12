package com.redmath.transfer.exception;

import com.redmath.exception_handler.exception.ResourceNotFoundException;

public class SenderAccountNotFoundException extends ResourceNotFoundException {
    public SenderAccountNotFoundException(String message) {
        super(message);
    }
}
