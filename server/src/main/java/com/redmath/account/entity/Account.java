package com.redmath.account.entity;

import com.redmath.account.balance.entity.Balance;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter @Setter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String name;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;
    private String password;
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToOne(
            mappedBy = "account",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private Balance balance;

    @Column(name = "password_change_required", nullable = false)
    private boolean passwordChangeRequired = false;
}
