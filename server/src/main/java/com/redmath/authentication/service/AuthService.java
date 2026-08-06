package com.redmath.authentication.service;

import com.redmath.account.Account;
import com.redmath.account.AccountRepository;
import com.redmath.account.AccountResponse;
import com.redmath.account.Role;
import com.redmath.authentication.dto.LoginAndRefreshResult;
import com.redmath.authentication.dto.LoginRequest;
import com.redmath.authentication.dto.RegisterRequest;
import com.redmath.authentication.entity.RefreshToken;
import com.redmath.authentication.exception.EmailAlreadyExistsException;
import com.redmath.authentication.wrapper.AccountPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AccountResponse register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Unable to register with the provided details");
        }

        Account account = new Account();
        account.setName(request.name());
        account.setEmail(request.email());
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setAddress(request.address());
        account.setRole(Role.USER);
        account.setCreatedAt(Instant.now());
        account.setUpdatedAt(Instant.now());

        Account saved = accountRepository.save(account);

        return new AccountResponse(
                saved.getUserId(), saved.getName(), saved.getEmail(), saved.getAddress(), saved.getRole().name());
    }

    @Transactional
    public LoginAndRefreshResult login(LoginRequest request) {
        Authentication authentication = authenticate(request);
        Account user = ((AccountPrincipal) authentication.getPrincipal()).account();
        RefreshToken refreshToken = refreshTokenService.issueRefreshToken(user);
        return buildLoginResult(authentication, refreshToken.getToken(), user);
    }

    @Transactional
    public LoginAndRefreshResult refresh(String oldToken) {
        RefreshToken refreshToken = refreshTokenService.verifyAndRotate(oldToken);
        Authentication authentication = createAuthentication(refreshToken.getUser());
        return buildLoginResult(authentication, refreshToken.getToken(), refreshToken.getUser());
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null) {
            return;
        }
        refreshTokenService.deleteByToken(refreshToken);
    }

    private Authentication authenticate(LoginRequest request) {
        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    }

    private Authentication createAuthentication(Account account) {
        AccountPrincipal principal = new AccountPrincipal(account);
        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );
    }

    private LoginAndRefreshResult buildLoginResult(Authentication authentication, String refreshToken, Account account) {
        return new LoginAndRefreshResult(
                jwtService.generateAccessToken(authentication),
                refreshToken,
                account.getUserId(),
                account.getEmail(),
                account.getRole().name()
        );
    }
}
