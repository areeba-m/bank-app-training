package com.redmath.authentication;

import com.redmath.account.entity.Account;
import com.redmath.authentication.entity.RefreshToken;
import com.redmath.authentication.exception.InvalidRefreshTokenException;
import com.redmath.authentication.repository.RefreshTokenRepository;
import com.redmath.authentication.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void verifyAndRotate_withExpiredToken_deletesTokenAndThrowsException() {
        Account mockUser = new Account();
        mockUser.setEmail("test@example.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("expired-token");
        refreshToken.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));
        refreshToken.setUser(mockUser);

        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(refreshToken));

        InvalidRefreshTokenException exception = assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshTokenService.verifyAndRotate("expired-token")
        );

        assertEquals("Refresh token expired", exception.getMessage());

        verify(refreshTokenRepository).findByToken("expired-token");
        verify(refreshTokenRepository).delete(refreshToken);
        verify(refreshTokenRepository, never()).save(any());
    }
}
