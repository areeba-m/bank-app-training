package com.redmath.admin;

import com.redmath.account.Role;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UpdateUserRequest;
import com.redmath.admin.dto.UserResponse;
import com.redmath.admin.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void createAccountShouldReturnCreatedResponse() {

        CreateUserRequest request = new CreateUserRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");
        request.setAddress("Lahore");

        UserResponse response = new UserResponse(
                1L,
                "Alice",
                "alice@example.com",
                "Lahore",
                Role.USER
        );

        when(adminService.createUser(request)).thenReturn(response);

        ResponseEntity<UserResponse> result =
                adminController.createAccount(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("Alice", result.getBody().getName());

        verify(adminService).createUser(request);
    }

    @Test
    void getAllAccountsShouldReturnList() {

        List<UserResponse> users = List.of(
                new UserResponse(1L, "Alice", "alice@example.com", "Lahore", Role.USER),
                new UserResponse(2L, "Bob", "bob@example.com", "Karachi", Role.ADMIN)
        );

        when(adminService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserResponse>> response =
                adminController.getAllAccounts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(adminService).getAllUsers();
    }

    @Test
    void getAccountByIdShouldReturnUser() {

        UserResponse user = new UserResponse(
                1L,
                "Alice",
                "alice@example.com",
                "Lahore",
                Role.USER
        );

        when(adminService.getUserById(1L)).thenReturn(user);

        ResponseEntity<UserResponse> response =
                adminController.getAccountById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Alice", response.getBody().getName());

        verify(adminService).getUserById(1L);
    }

    @Test
    void updateAccountShouldReturnUpdatedUser() {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Alice Updated");
        request.setAddress("Islamabad");

        UserResponse updated = new UserResponse(
                1L,
                "Alice Updated",
                "alice@example.com",
                "Islamabad",
                Role.USER
        );

        when(adminService.updateUser(1L, request)).thenReturn(updated);

        ResponseEntity<UserResponse> response =
                adminController.updateAccount(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Alice Updated", response.getBody().getName());
        assertEquals("Islamabad", response.getBody().getAddress());

        verify(adminService).updateUser(1L, request);
    }

    @Test
    void deleteAccountShouldReturnNoContent() {

        doNothing().when(adminService).deleteUser(1L);

        ResponseEntity<Void> response =
                adminController.deleteAccount(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(adminService).deleteUser(1L);
    }
}