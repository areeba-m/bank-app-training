package com.redmath.account.exception;

import com.redmath.exception_utility.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }

}
