package com.redmath.transfer.entity;

import com.redmath.account.entity.Account;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transfers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_transfer_sender_idempotency", columnNames = {"sender_account_id", "idempotency_key"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant date;

    private String description;

    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id")
    private Account senderAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id")
    private Account receiverAccount;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;
}
