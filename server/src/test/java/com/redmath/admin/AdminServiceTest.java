package com.redmath.admin;

import com.redmath.account.dto.AccountResponse;
import com.redmath.account.entity.Account;
import com.redmath.account.entity.Role;
import com.redmath.account.exception.UserNotFoundException;
import com.redmath.account.mapper.AccountMapper;
import com.redmath.account.repository.AccountRepository;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UpdateUserRequest;
import com.redmath.admin.service.AdminService;
import com.redmath.authentication.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AccountRepository userRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AdminService adminService;

    private CreateUserRequest validCreateRequest;
    private UpdateUserRequest validUpdateRequest;
    private Account existingUser;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        validCreateRequest = new CreateUserRequest(
                "Alice Johnson",
                "alice@example.com",
                "password123",
                "123 Maple Street"
        );

        validUpdateRequest = new UpdateUserRequest(
                "Alice Updated",
                "456 Oak Avenue"
        );

        existingUser = new Account();
        existingUser.setUserId(1L);
        existingUser.setName("Alice Johnson");
        existingUser.setEmail("alice@example.com");
        existingUser.setPassword("password123");
        existingUser.setAddress("123 Maple Street");
        existingUser.setRole(Role.USER);

        accountResponse = new AccountResponse(
                1L,
                "Alice Johnson",
                "alice@example.com",
                "123 Maple Street",
                Role.USER,
                new BigDecimal("0.00")
        );
    }

    // --- createUser ---

    @Test
    void createUserShouldCreateUserWhenEmailDoesNotExist() {
        // Given
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(accountMapper.toEntity(validCreateRequest)).thenReturn(existingUser);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(accountMapper.toResponse(existingUser)).thenReturn(accountResponse);

        // When
        AccountResponse result = adminService.createUser(validCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Alice Johnson");
        assertThat(result.email()).isEqualTo("alice@example.com");

        verify(userRepository).existsByEmail("alice@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> adminService.createUser(validCreateRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Unable to register with the provided details");

        verify(userRepository, never()).save(any());
    }

    // --- getAllUsers ---

    @Test
    void getAllUsersShouldReturnAllUsersAsResponsePage() {

        // Given
        Account user1 = new Account();
        user1.setUserId(1L);
        user1.setEmail("alice@example.com");

        Account user2 = new Account();
        user2.setUserId(2L);
        user2.setEmail("bob@example.com");

        Page<Account> accountPage = new PageImpl<>(
                List.of(user1, user2),
                PageRequest.of(0, 10),
                2
        );

        when(userRepository.findByRole(eq(Role.USER), any(Pageable.class))).thenReturn(accountPage);

        when(accountMapper.toResponse(user1))
                .thenReturn(new AccountResponse(1L, "Alice", "alice@example.com",
                        "Addr1", Role.USER, new BigDecimal("0.00")));

        when(accountMapper.toResponse(user2))
                .thenReturn(new AccountResponse(2L, "Bob", "bob@example.com",
                        "Addr2", Role.USER, new BigDecimal("0.00")));

        // When
        Page<AccountResponse> result = adminService.getAllUsers(0, 10);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).name()).isEqualTo("Alice");
        assertThat(result.getContent().get(1).name()).isEqualTo("Bob");
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(userRepository).findByRole(eq(Role.USER), any(Pageable.class));
    }

    @Test
    void getAllUsersShouldReturnEmptyPageWhenNoUsers() {

        // Given
        Page<Account> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(userRepository.findByRole(Role.USER, PageRequest.of(0, 10))).thenReturn(emptyPage);

        // When
        Page<AccountResponse> result = adminService.getAllUsers(0, 10);

        // Then
        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(userRepository).findByRole(Role.USER, PageRequest.of(0, 10));
    }

    // --- getUserById ---

    @Test
    void getUserByIdShouldReturnUserResponseWhenFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(accountMapper.toResponse(existingUser)).thenReturn(accountResponse);

        // When
        AccountResponse result = adminService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("alice@example.com");
    }

    @Test
    void getUserByIdShouldThrowUserNotFoundExceptionWhenNotFound() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> adminService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id 99 not found.");

        verify(accountMapper, never()).toResponse(any());
    }

    // --- updateUser ---

    @Test
    void updateUserShouldUpdateNameAndAddressWhenUserFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(accountMapper.toResponse(existingUser)).thenReturn(accountResponse);

        // When
        AccountResponse result = adminService.updateUser(1L, validUpdateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(existingUser.getName()).isEqualTo("Alice Updated");
        assertThat(existingUser.getAddress()).isEqualTo("456 Oak Avenue");

        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUserShouldUpdateOnlyNameWhenAddressIsBlank() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(
                "Alice NewName",
                ""
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(accountMapper.toResponse(existingUser)).thenReturn(accountResponse);

        // When
        AccountResponse result = adminService.updateUser(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(existingUser.getName()).isEqualTo("Alice NewName");
        assertThat(existingUser.getAddress()).isEqualTo("123 Maple Street"); // unchanged
    }

    @Test
    void updateUserShouldThrowUserNotFoundExceptionWhenNotFound() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> adminService.updateUser(99L, validUpdateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id 99 not found.");

        verify(userRepository, never()).save(any());
    }

    // --- deleteUser ---

    @Test
    void deleteUserShouldDeleteUserWhenFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        adminService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(existingUser);
    }

    @Test
    void deleteUserShouldThrowUserNotFoundExceptionWhenNotFound() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> adminService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id 99 not found.");

        verify(userRepository, never()).delete(any());
    }
}