package com.redmath.admin;

import com.redmath.account.Account;
import com.redmath.account.Role;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UserResponse;
import com.redmath.admin.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    private CreateUserRequest validRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Alice Johnson");
        request.setEmail("alice@example.com");
        request.setPassword("password123");
        request.setAddress("123 Maple Street");
        return request;
    }

    @Test
    void toEntityShouldMapAllFieldsFromCreateUserRequest() {
        // Given
        CreateUserRequest request = validRequest();

        // When
        Account account = userMapper.toEntity(request);

        // Then
        assertThat(account.getName()).isEqualTo("Alice Johnson");
        assertThat(account.getEmail()).isEqualTo("alice@example.com");
        assertThat(account.getPassword()).isEqualTo("password123");
        assertThat(account.getAddress()).isEqualTo("123 Maple Street");
    }

    @Test
    void toEntityShouldSetRoleToUser() {
        // Given
        CreateUserRequest request = validRequest();

        // When
        Account account = userMapper.toEntity(request);

        // Then
        assertThat(account.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void toEntityShouldSetCreatedAt() {
        // Given
        CreateUserRequest request = validRequest();
        Instant before = Instant.now();

        // When
        Account account = userMapper.toEntity(request);

        // Then
        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getCreatedAt()).isBetween(before, Instant.now());
    }

    @Test
    void toEntityShouldSetUpdatedAt() {
        // Given
        CreateUserRequest request = validRequest();
        Instant before = Instant.now();

        // When
        Account account = userMapper.toEntity(request);

        // Then
        assertThat(account.getUpdatedAt()).isNotNull();
        assertThat(account.getUpdatedAt()).isBetween(before, Instant.now());
    }

    @Test
    void toEntityShouldReturnNewAccountInstance() {
        // Given
        CreateUserRequest request = validRequest();

        // When
        Account account = userMapper.toEntity(request);

        // Then
        assertThat(account).isNotNull();
        assertThat(account.getUserId()).isNull();
    }

    @Test
    void toResponseShouldMapAllFieldsFromAccount() {
        // Given
        Account account = new Account();
        account.setUserId(1L);
        account.setName("Alice Johnson");
        account.setEmail("alice@example.com");
        account.setAddress("123 Maple Street");
        account.setRole(Role.USER);

        // When
        UserResponse response = userMapper.toResponse(account);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice Johnson");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getAddress()).isEqualTo("123 Maple Street");
        assertThat(response.getRole()).isEqualTo(Role.USER);
    }
}