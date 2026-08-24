package com.redmath.transfer.exception;

import com.redmath.exception_utility.exception.ResourceNotFoundException;

public class SenderAccountNotFoundException extends ResourceNotFoundException {
    public SenderAccountNotFoundException(String message) {
        super(message);
    }
}
