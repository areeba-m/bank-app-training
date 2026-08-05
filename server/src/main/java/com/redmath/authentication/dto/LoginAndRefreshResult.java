package com.redmath.authentication.dto;

public record LoginAndRefreshResult(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,
        String role
) {}
