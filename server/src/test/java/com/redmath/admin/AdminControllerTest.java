package com.redmath.admin;

import com.redmath.account.dto.AccountResponse;
import com.redmath.account.entity.Role;
import com.redmath.admin.controller.AdminController;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UpdateUserRequest;
import com.redmath.admin.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void createAccountShouldReturnCreatedResponse() {

        CreateUserRequest request = new CreateUserRequest(
                "Alice",
                "alice@example.com",
                "password123",
                "Lahore"
        );

        AccountResponse response = new AccountResponse(
                1L,
                "Alice",
                "alice@example.com",
                "Lahore",
                Role.USER
        );

        when(adminService.createUser(request)).thenReturn(response);

        ResponseEntity<AccountResponse> result =
                adminController.createAccount(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Alice", result.getBody().name());

        verify(adminService).createUser(request);
    }

    @Test
    void getAllAccountsShouldReturnList() {

        List<AccountResponse> users = List.of(
                new AccountResponse(1L, "Alice", "alice@example.com", "Lahore", Role.USER),
                new AccountResponse(2L, "Bob", "bob@example.com", "Karachi", Role.ADMIN)
        );

        Page<AccountResponse> userPage =
                new PageImpl<>(users, PageRequest.of(0, 10), users.size());

        when(adminService.getAllUsers(0, 10)).thenReturn(userPage);

        ResponseEntity<Page<AccountResponse>> response =
                adminController.getAllAccounts(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals(1, response.getBody().getTotalPages());

        verify(adminService).getAllUsers(0, 10);
    }

    @Test
    void getAccountByIdShouldReturnUser() {

        AccountResponse user = new AccountResponse(
                1L,
                "Alice",
                "alice@example.com",
                "Lahore",
                Role.USER
        );

        when(adminService.getUserById(1L)).thenReturn(user);

        ResponseEntity<AccountResponse> response =
                adminController.getAccountById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Alice", response.getBody().name());

        verify(adminService).getUserById(1L);
    }

    @Test
    void updateAccountShouldReturnUpdatedUser() {

        UpdateUserRequest request = new UpdateUserRequest(
                "Alice Updated",
                "Islamabad"
        );

        AccountResponse updated = new AccountResponse(
                1L,
                "Alice Updated",
                "alice@example.com",
                "Islamabad",
                Role.USER
        );

        when(adminService.updateUser(1L, request)).thenReturn(updated);

        ResponseEntity<AccountResponse> response =
                adminController.updateAccount(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Alice Updated", response.getBody().name());
        assertEquals("Islamabad", response.getBody().address());

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