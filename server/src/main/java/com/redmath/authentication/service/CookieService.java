package com.redmath.authentication.service;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {
    boolean isHttpOnly = true;
    boolean isSecure = true;
    String sameSite = "none";
    String refreshPath = "/api/v1/auth/refresh";

    public ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(isHttpOnly)
                .secure(isSecure)
                .sameSite(sameSite)
                .path(refreshPath)
                .maxAge(Duration.ofDays(7))
                .partitioned(true)
                .build();
    }

    public ResponseCookie deleteRefreshCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(isHttpOnly)
                .secure(isSecure)
                .sameSite(sameSite)
                .path(refreshPath)
                .maxAge(Duration.ZERO)
                .build();
    }
}
