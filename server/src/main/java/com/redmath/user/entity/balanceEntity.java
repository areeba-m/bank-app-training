package com.redmath.user.entity;

import com.redmath.account.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Getter
@Setter
@Table(name = "balance")
public class balanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant date;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private Indicator indicator;
    @OneToOne
    @JoinColumn(name = "user_id")
    private Account account;
    @PrePersist
    public void prePersist() {
        this.date = Instant.now();
    }
}