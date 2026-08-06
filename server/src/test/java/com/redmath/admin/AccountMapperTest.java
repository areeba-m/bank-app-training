package com.redmath.admin;

import com.redmath.account.entity.Account;
import com.redmath.account.entity.Role;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.account.dto.AccountResponse;
import com.redmath.account.mapper.AccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private AccountMapper accountMapper;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper(new BCryptPasswordEncoder());
    }

    private CreateUserRequest validRequest() {
        return new CreateUserRequest(
                "Alice Johnson",
                "alice@example.com",
                "password123",
                "123 Maple Street"
        );
    }

    @Test
    void toEntityShouldMapAllFieldsFromCreateUserRequest() {
        // Given
        CreateUserRequest request = validRequest();

        // When
        Account account = accountMapper.toEntity(request);

        // Then
        assertThat(account.getName()).isEqualTo("Alice Johnson");
        assertThat(account.getEmail()).isEqualTo("alice@example.com");
        assertThat(account.getAddress()).isEqualTo("123 Maple Street");

        // Verify password was encoded
        assertThat(account.getPassword()).isNotEqualTo("password123");
        assertThat(new BCryptPasswordEncoder().matches(
                "password123",
                account.getPassword()
        )).isTrue();
    }

    @Test
    void toEntityShouldSetRoleToUser() {
        CreateUserRequest request = validRequest();

        Account account = accountMapper.toEntity(request);

        assertThat(account.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void toEntityShouldSetCreatedAt() {
        CreateUserRequest request = validRequest();
        Instant before = Instant.now();

        Account account = accountMapper.toEntity(request);

        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getCreatedAt()).isBetween(before, Instant.now());
    }

    @Test
    void toEntityShouldSetUpdatedAt() {
        CreateUserRequest request = validRequest();
        Instant before = Instant.now();

        Account account = accountMapper.toEntity(request);

        assertThat(account.getUpdatedAt()).isNotNull();
        assertThat(account.getUpdatedAt()).isBetween(before, Instant.now());
    }

    @Test
    void toEntityShouldReturnNewAccountInstance() {
        CreateUserRequest request = validRequest();

        Account account = accountMapper.toEntity(request);

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
        AccountResponse response = accountMapper.toResponse(account);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Alice Johnson");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.address()).isEqualTo("123 Maple Street");
        assertThat(response.role()).isEqualTo(Role.USER);
    }
}