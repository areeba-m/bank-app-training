package com.redmath.account;

public record AccountResponse(
        Long userId,
        String name,
        String email,
        String role
) {}
