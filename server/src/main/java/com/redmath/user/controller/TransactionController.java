package com.redmath.user.controller;


import com.redmath.user.dto.CreateTransactionRequest;
import com.redmath.user.dto.TransactionResponse;
import com.redmath.user.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid
            @RequestBody CreateTransactionRequest request
    ){
        return ResponseEntity.ok(transactionService.createTransaction(request));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @PathVariable Long accountId){
        return ResponseEntity.ok(
                transactionService.getTransactions(
                        accountId
                )
        );

    }
}