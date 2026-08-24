package com.redmath.admin.controller;

import com.redmath.account.dto.AccountResponse;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UpdateUserRequest;
import com.redmath.admin.service.AdminService;
import com.redmath.account.balance.dto.BalanceResponse;
import com.redmath.account.balance.service.BalanceService;
import com.redmath.transfer.transactions.dto.TransactionResponse;
import com.redmath.transfer.transactions.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final TransactionService transactionService;
    private final BalanceService balanceService;
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateUserRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.createUser(request));
    }

    @GetMapping
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id) {

        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
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

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(@RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                transactionService.getTransactions(
                        userId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                balanceService.getBalance(userId)
        );
    }
}
