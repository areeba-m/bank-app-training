package com.redmath.authentication.controller;

import com.redmath.account.dto.AccountResponse;
import com.redmath.authentication.dto.*;
import com.redmath.authentication.service.AuthService;
import com.redmath.authentication.service.CookieService;
import com.redmath.authentication.service.OtpService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final CookieService cookieService;
    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, @NonNull HttpServletResponse response) {
        LoginAndRefreshResult result = authService.login(request);
        ResponseCookie cookie = cookieService.buildRefreshCookie(result.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new AuthResponse(result.accessToken(), result.userId(), result.email(), result.role()));
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> getCsrfToken(CsrfToken token) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue("refresh_token") String refreshToken,
                                                @NonNull HttpServletResponse response) {
        LoginAndRefreshResult result = authService.refresh(refreshToken);
        ResponseCookie cookie = cookieService.buildRefreshCookie(result.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new AuthResponse(result.accessToken(), result.userId(), result.email(), result.role()));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<OtpVerifyResponse> verifyOtp(@RequestBody @Valid OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/password/change")
    public ResponseEntity<Void> setNewPassword(@RequestBody @Valid SetNewPasswordRequest request) {
        authService.setNewPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/otp/resend")
    public ResponseEntity<Void> resendOtp(@RequestBody @Valid ResendOtpRequest request) {
        otpService.resendOtp(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refresh_token", required = false) String refreshToken,
                                       @NonNull HttpServletResponse response) {
        authService.logout(refreshToken);

        ResponseCookie cookie = cookieService.deleteRefreshCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.noContent().build();
    }
}
