package com.redmath.account.dto;

import com.redmath.account.entity.Role;

public record AccountResponse(
        Long id,
        String name,
        String email,
        String address,
        Role role
) {
}
