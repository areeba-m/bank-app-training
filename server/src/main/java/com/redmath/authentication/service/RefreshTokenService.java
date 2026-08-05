package com.redmath.authentication.service;

import com.redmath.account.Account;
import com.redmath.authentication.entity.RefreshToken;
import com.redmath.authentication.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken issueRefreshToken(Account user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> {
                    RefreshToken token = new RefreshToken();
                    token.setUser(user);
                    return token;
                });

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyAndRotate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadCredentialsException("Refresh token expired");
        }

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        return refreshToken;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
    }

    public void deleteByToken(String token){
        refreshTokenRepository.deleteByToken(token);
    }
}
