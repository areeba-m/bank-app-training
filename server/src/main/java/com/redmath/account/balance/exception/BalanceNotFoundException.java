package com.redmath.account.balance.exception;

import com.redmath.exception_utility.exception.ResourceNotFoundException;

public class BalanceNotFoundException extends ResourceNotFoundException {

    public BalanceNotFoundException(String message)
    {
        super(message);
    }
}