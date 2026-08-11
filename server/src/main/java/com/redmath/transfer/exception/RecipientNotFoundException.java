package com.redmath.transfer.exception;

import com.redmath.exception_handler.exception.ResourceNotFoundException;

public class RecipientNotFoundException extends ResourceNotFoundException {
    public RecipientNotFoundException(String message) {
        super(message);
    }
}
