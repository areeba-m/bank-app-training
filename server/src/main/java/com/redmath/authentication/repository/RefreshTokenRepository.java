package com.redmath.authentication.repository;

import com.redmath.account.entity.Account;
import com.redmath.authentication.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(Account user);

    Long deleteByToken(String token);
    Long deleteAllByExpiryDateBefore(Instant instant);
    void deleteAllByUser(Account user);
}