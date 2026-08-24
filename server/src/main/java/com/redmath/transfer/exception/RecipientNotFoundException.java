package com.redmath.transfer.exception;

import com.redmath.exception_utility.exception.ResourceNotFoundException;

public class RecipientNotFoundException extends ResourceNotFoundException {
    public RecipientNotFoundException(String message) {
        super(message);
    }
}
