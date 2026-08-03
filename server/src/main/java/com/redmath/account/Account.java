package com.redmath.account;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class Account {
    @Id
    private Long userId;
    private String email;
    private String password;
    private String address;
    private String role;
    private Instant createdAt;
    private Instant updatedAt;
}
