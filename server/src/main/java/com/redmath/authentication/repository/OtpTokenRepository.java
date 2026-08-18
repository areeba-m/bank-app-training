package com.redmath.authentication.repository;

import com.redmath.account.entity.Account;
import com.redmath.authentication.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByAccountAndUsedFalseOrderByCreatedAtDesc(Account account);
    Optional<OtpToken> findTopByAccountOrderByCreatedAtDesc(Account account);
}