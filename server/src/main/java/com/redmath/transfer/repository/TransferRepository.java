package com.redmath.transfer.repository;

import com.redmath.transfer.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @Query("select t from Transfer t "
            + "where t.senderAccount.userId = :userId or t.receiverAccount.userId = :userId")
    Page<Transfer> findAllByAccountUserId(@Param("userId") Long userId, Pageable pageable);

    Page<Transfer> findBySenderAccountUserId(Long userId, Pageable pageable);

    Page<Transfer> findByReceiverAccountUserId(Long userId, Pageable pageable);
}
