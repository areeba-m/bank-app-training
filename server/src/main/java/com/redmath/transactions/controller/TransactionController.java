package com.redmath.transactions.controller;

import com.redmath.transactions.dto.CreateTransactionRequest;
import com.redmath.transactions.dto.TransactionResponse;
import com.redmath.transactions.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;


    @PostMapping()
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @NonNull Authentication auth) {

        String email = auth.getName();

        return ResponseEntity.ok(
                transactionService.createTransaction(request, email)
        );
    }


    @GetMapping()
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                transactionService.getTransactions(
                        authentication.getName(),
                        page,
                        size
                )
        );
    }
}