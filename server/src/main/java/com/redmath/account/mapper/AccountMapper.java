package com.redmath.account.mapper;

import com.redmath.account.entity.Account;
import com.redmath.account.entity.Role;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.account.dto.AccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AccountMapper {

    private final PasswordEncoder passwordEncoder;

    public Account toEntity(CreateUserRequest request) {

        Account user = new Account();

        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAddress(request.address());
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        return user;
    }

    public AccountResponse toResponse(Account user) {

        return new AccountResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getAddress(),
                user.getRole()
        );
    }
}