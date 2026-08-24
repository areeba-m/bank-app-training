package com.redmath.transfer.transactions.repository;

import com.redmath.account.entity.Account;
import com.redmath.transfer.transactions.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccountUserId(Long userId, Pageable pageable);
    Optional<Transaction> findByAccountAndIdempotencyKey(Account account, String idempotencyKey);
}