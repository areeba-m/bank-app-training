package com.redmath.balance.exception;

import com.redmath.exception_handler.exception.ResourceNotFoundException;

public class BalanceNotFoundException extends ResourceNotFoundException {

    public BalanceNotFoundException(String message)
    {
        super(message);
    }
}