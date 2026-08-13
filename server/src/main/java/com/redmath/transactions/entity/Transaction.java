package com.redmath.transactions.entity;

import com.redmath.account.entity.Account;
import com.redmath.transfer.entity.Transfer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_transaction_account_idempotency", columnNames = {"account_id", "idempotency_key"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant date;
    private String description;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Indicator indicator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_account_id")
    private Account counterpartyAccount;
    String counterpartyName;
    String counterpartyEmail;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transfer_id")
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Transfer transfer;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

}