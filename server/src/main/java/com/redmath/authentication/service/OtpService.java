package com.redmath.authentication.service;

import com.redmath.account.entity.Account;
import com.redmath.account.exception.UserNotFoundException;
import com.redmath.account.repository.AccountRepository;
import com.redmath.authentication.dto.ResendOtpRequest;
import com.redmath.authentication.entity.OtpToken;
import com.redmath.authentication.exception.OtpCooldownException;
import com.redmath.authentication.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    private final OtpTokenRepository otpTokenRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.otp.length:6}")
    private int otpLength;
    @Value("${app.otp.expiry-days:1}")
    private long expiryDays;
    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;
    @Value("${app.otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void generateAndSendOtp(Account account) {
        String otp = generateNumericOtp();

        OtpToken token = new OtpToken();
        token.setAccount(account);
        token.setOtpHash(passwordEncoder.encode(otp));
        token.setExpiresAt(Instant.now().plus(expiryDays, ChronoUnit.DAYS));
        token.setCreatedAt(Instant.now());
        otpTokenRepository.save(token);

        emailService.sendOtpEmail(account.getEmail(), account.getName(), otp, expiryDays);
        log.info("OTP issued for userId={}", account.getUserId());
    }

    @Transactional
    public Account verifyOtp(String email, String otp) {

        log.info("========== OTP VERIFICATION START ==========");
        log.info("Email received: [{}]", email);
        log.info("OTP received: [{}]", otp);

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("ACCOUNT NOT FOUND for email: [{}]", email);
                    return new UserNotFoundException("Invalid OTP or email");
                });

        log.info(
                "Account found: userId={}, email={}",
                account.getUserId(),
                account.getEmail()
        );

        OtpToken token = otpTokenRepository
                .findTopByAccountAndUsedFalseOrderByCreatedAtDesc(account)
                .orElseThrow(() -> {
                    log.error(
                            "NO UNUSED OTP FOUND for account userId={}",
                            account.getUserId()
                    );
                    return new BadCredentialsException("Invalid OTP or email");
                });

        log.info("OTP token found");
        log.info("Token createdAt: {}", token.getCreatedAt());
        log.info("Token expiresAt: {}", token.getExpiresAt());
        log.info("Token used: {}", token.isUsed());
        log.info("Token attempts: {}", token.getAttempts());

        if (token.isUsed()) {
            log.error("OTP IS ALREADY USED");
            throw new BadCredentialsException("OTP expired or already used");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            log.error("OTP IS EXPIRED");
            throw new BadCredentialsException("OTP expired or already used");
        }

        if (token.getAttempts() >= maxAttempts) {
            log.error("OTP MAXIMUM ATTEMPTS REACHED");
            throw new LockedException(
                    "Too many attempts. Request a new OTP."
            );
        }

        token.setAttempts(token.getAttempts() + 1);

        boolean matches = passwordEncoder.matches(
                otp,
                token.getOtpHash()
        );

        log.info("OTP matches stored hash: {}", matches);

        if (!matches) {
            otpTokenRepository.save(token);

            log.error("OTP DOES NOT MATCH");

            throw new BadCredentialsException(
                    "Invalid OTP or email"
            );
        }

        token.setUsed(true);
        otpTokenRepository.save(token);

        log.info("OTP VERIFIED SUCCESSFULLY");
        log.info("========== OTP VERIFICATION END ==========");

        return account;
    }

    private String generateNumericOtp() {
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    @Transactional
    public void resendOtp(@NonNull ResendOtpRequest request) {
        Account account = accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid request"));

        otpTokenRepository.findTopByAccountOrderByCreatedAtDesc(account)
                .ifPresent(lastToken -> {
                    Instant cooldownEnd = lastToken.getCreatedAt().plusSeconds(resendCooldownSeconds);
                    if (Instant.now().isBefore(cooldownEnd)) {
                        long secondsLeft = Duration.between(Instant.now(), cooldownEnd).getSeconds();
                        throw new OtpCooldownException(
                                "Please wait before requesting another OTP", secondsLeft);
                    }
                });

        // invalidate any still-active OTP before issuing a new one
        otpTokenRepository.findTopByAccountAndUsedFalseOrderByCreatedAtDesc(account)
                .ifPresent(token -> {
                    token.setUsed(true);
                    otpTokenRepository.save(token);
                });

        generateAndSendOtp(account);
    }
}