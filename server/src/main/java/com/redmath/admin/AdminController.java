package com.redmath.admin;

import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UpdateUserRequest;
import com.redmath.admin.dto.UserResponse;
import com.redmath.admin.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/accounts")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createAccount(
            @Valid @RequestBody CreateUserRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.createUser(request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllAccounts() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getAccountById(
            @PathVariable Long id) {

        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(
                adminService.updateUser(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long id) {

        adminService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
