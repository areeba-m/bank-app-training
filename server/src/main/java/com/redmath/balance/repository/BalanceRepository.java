package com.redmath.balance.repository;

import com.redmath.balance.entity.Balance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long>
{
    Optional<Balance> findByAccountUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Balance b where b.account.userId = :userId")
    Optional<Balance> findByAccountUserIdForUpdate(@Param("userId") Long userId);
}