package com.redmath.transfer.transactions.controller;

import com.redmath.transfer.transactions.dto.CreateTransactionRequest;
import com.redmath.transfer.transactions.dto.TransactionResponse;
import com.redmath.transfer.transactions.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/user/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;


    @PostMapping()
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateTransactionRequest request,
            @NonNull Authentication auth) {

        String email = auth.getName();

        return ResponseEntity.ok()
                .header("Idempotency-Key", idempotencyKey)
                .body(transactionService.createTransaction(request, email, idempotencyKey));
    }


    @GetMapping()
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @NonNull Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = ((Jwt) Objects.requireNonNull(authentication.getPrincipal())).getClaim("userId");

        return ResponseEntity.ok(
                transactionService.getTransactions(
                        userId,
                        page,
                        size
                )
        );
    }
}