package com.redmath.authentication.repository;

import com.redmath.account.entity.Account;
import com.redmath.authentication.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(Account user);

    void deleteByToken(String token);
    void deleteAllByExpiryDateBefore(Instant instant);
}