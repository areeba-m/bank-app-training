package com.redmath.admin.mapper;

import com.redmath.account.Account;
import com.redmath.account.Role;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserMapper {

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

    public UserResponse toResponse(Account user) {

        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getAddress(),
                user.getRole()
        );
    }
}