package com.redmath.user.repository;

import com.redmath.user.entity.balanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface balanceRepository
        extends JpaRepository<balanceEntity,Long> {
    Optional<balanceEntity> findByAccountUserId(Long userId);

}