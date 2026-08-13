package com.redmath.account.dto;

import com.redmath.account.entity.Role;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String name,
        String email,
        String address,
        Role role,
        BigDecimal balance
) {
}
