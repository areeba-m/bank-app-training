package com.redmath.categorization.entity;

import com.redmath.transactions.entity.Transaction;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "transaction_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private CategorySource categorySource;

    private Double confidence;

    private Instant categorizedAt;

    @PrePersist
    public void prePersist() {
        this.categorizedAt = Instant.now();
    }
}
