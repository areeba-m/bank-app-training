package com.redmath.admin;

import com.redmath.account.Account;
import com.redmath.account.AccountRepository;
import com.redmath.account.Role;
import com.redmath.account.exception.UserAlreadyExistsException;
import com.redmath.account.exception.UserNotFoundException;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UpdateUserRequest;
import com.redmath.admin.dto.UserResponse;
import com.redmath.admin.mapper.UserMapper;
import com.redmath.admin.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AccountRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminService adminService;

    private CreateUserRequest validCreateRequest;
    private UpdateUserRequest validUpdateRequest;
    private Account existingUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        validCreateRequest = new CreateUserRequest();
        validCreateRequest.setName("Alice Johnson");
        validCreateRequest.setEmail("alice@example.com");
        validCreateRequest.setPassword("password123");
        validCreateRequest.setAddress("123 Maple Street");

        validUpdateRequest = new UpdateUserRequest();
        validUpdateRequest.setName("Alice Updated");
        validUpdateRequest.setAddress("456 Oak Avenue");

        existingUser = new Account();
        existingUser.setUserId(1L);
        existingUser.setName("Alice Johnson");
        existingUser.setEmail("alice@example.com");
        existingUser.setPassword("password123");
        existingUser.setAddress("123 Maple Street");
        existingUser.setRole(Role.USER);

        userResponse = new UserResponse(
                1L, "Alice Johnson", "alice@example.com", "123 Maple Street", Role.USER
        );
    }

    // --- createUser ---

    @Test
    void createUserShouldCreateUserWhenEmailDoesNotExist() {
        // Given
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userMapper.toEntity(validCreateRequest)).thenReturn(existingUser);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponse(existingUser)).thenReturn(userResponse);

        // When
        UserResponse result = adminService.createUser(validCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Alice Johnson");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        verify(userRepository).existsByEmail("alice@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> adminService.createUser(validCreateRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email 'alice@example.com' already exists.");

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

        when(userRepository.findAll(any(Pageable.class))).thenReturn(accountPage);

        when(userMapper.toResponse(user1)).thenReturn(
                new UserResponse(1L, "Alice", "alice@example.com", "Addr1", Role.USER));

        when(userMapper.toResponse(user2)).thenReturn(
                new UserResponse(2L, "Bob", "bob@example.com", "Addr2", Role.USER));

        // When
        Page<UserResponse> result = adminService.getAllUsers(0, 10);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alice");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Bob");
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(userRepository).findAll(any(Pageable.class));
    }


    @Test
    void getAllUsersShouldReturnEmptyPageWhenNoUsers() {

        // Given
        Page<Account> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(emptyPage);

        // When
        Page<UserResponse> result = adminService.getAllUsers(0, 10);

        // Then
        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(userRepository).findAll(PageRequest.of(0, 10));
    }

    // --- getUserById ---

    @Test
    void getUserByIdShouldReturnUserResponseWhenFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userMapper.toResponse(existingUser)).thenReturn(userResponse);

        // When
        UserResponse result = adminService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void getUserByIdShouldThrowUserNotFoundExceptionWhenNotFound() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> adminService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id 99 not found.");

        verify(userMapper, never()).toResponse(any());
    }

    // --- updateUser ---

    @Test
    void updateUserShouldUpdateNameAndAddressWhenUserFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponse(existingUser)).thenReturn(userResponse);

        // When
        UserResponse result = adminService.updateUser(1L, validUpdateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(existingUser.getName()).isEqualTo("Alice Updated");
        assertThat(existingUser.getAddress()).isEqualTo("456 Oak Avenue");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUserShouldUpdateOnlyNameWhenAddressIsBlank() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Alice NewName");
        request.setAddress("");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponse(existingUser)).thenReturn(userResponse);

        // When
        UserResponse result = adminService.updateUser(1L, request);

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