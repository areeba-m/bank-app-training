package com.redmath.admin.dto;

import com.redmath.account.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        String address,
        Role role
) {
}
