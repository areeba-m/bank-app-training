package com.redmath.authentication.service;

import com.redmath.account.entity.Account;
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

    public RefreshToken issueRefreshToken(Account user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> {
                    RefreshToken token = new RefreshToken();
                    token.setUser(user);
                    return token;
                });

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));

        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        log.info("Issued refresh token for user '{}'.", user.getEmail());

        return saved;
    }

    public RefreshToken verifyAndRotate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Refresh token validation failed: token not found.");
                    return new BadCredentialsException("Invalid refresh token");
                });

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Expired refresh token used by user '{}'.", refreshToken.getUser().getEmail());

            refreshTokenRepository.delete(refreshToken);
            throw new BadCredentialsException("Refresh token expired");
        }

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        return refreshToken;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        Long count = refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
        log.info("Deleted {} expired refresh tokens during scheduled cleanup.", count);
    }

    public void deleteByToken(String token){
        Long count = refreshTokenRepository.deleteByToken(token);
        log.info("Deleted {} refresh tokens", count);
    }
}
