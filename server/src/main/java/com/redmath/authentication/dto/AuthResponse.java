package com.redmath.authentication.dto;

public record AuthResponse(
        String accessToken,
        Long userId,
        String email,
        String role
) {}