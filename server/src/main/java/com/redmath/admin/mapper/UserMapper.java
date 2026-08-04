package com.redmath.admin.mapper;


import com.redmath.account.Account;
import com.redmath.account.Role;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserMapper {

    public Account toEntity(CreateUserRequest request) {

        Account user = new Account();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setAddress(request.getAddress());
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
