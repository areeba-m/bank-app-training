package com.redmath.authentication.service;

import com.redmath.account.entity.Account;
import com.redmath.authentication.exception.InvalidTokenScopeException;
import com.redmath.authentication.wrapper.AccountPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    @Value("${app.jwt.password-reset-token-expiry-minutes:10}")
    private int passwordResetExpiryMinutes;

    public String generateAccessToken(@NonNull Authentication authentication) {
        Instant now = Instant.now();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Long userId = ((AccountPrincipal) Objects.requireNonNull(authentication.getPrincipal())).getUserId();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .claim("userId", userId)
                .claim("roles", roles)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generatePasswordResetToken(Account account) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(account.getEmail())
                .claim("userId", account.getUserId())
                .claim("purpose", "PASSWORD_RESET")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(passwordResetExpiryMinutes, ChronoUnit.MINUTES))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Jwt parsePasswordResetToken(@NonNull String token) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(token);
        } catch (JwtValidationException e) {
            log.debug("Password reset token failed validation: {}", e.getMessage());
            throw new InvalidTokenScopeException("Reset link has expired or is invalid");
        } catch (JwtException e) {
            log.debug("Password reset token could not be decoded: {}", e.getMessage());
            throw new InvalidTokenScopeException("Invalid reset token");
        }

        String purpose = jwt.getClaimAsString("purpose");
        if (!"PASSWORD_RESET".equals(purpose)) {
            log.warn("Token with wrong purpose '{}' presented to password reset endpoint", purpose);
            throw new InvalidTokenScopeException("Token is not valid for password reset");
        }

        return jwt;
    }

}
