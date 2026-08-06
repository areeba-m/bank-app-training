package com.redmath.account;

import com.redmath.user.entity.TransactionEntity;
import com.redmath.user.entity.balanceEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private balanceEntity balance;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<TransactionEntity> transactions = new ArrayList<>();
}
