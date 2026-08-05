package com.redmath.user.repository;

import com.redmath.user.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface TransactionRepository
        extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByAccountUserId(Long accountId);

}