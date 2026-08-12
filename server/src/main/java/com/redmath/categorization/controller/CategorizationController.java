package com.redmath.categorization.controller;

import com.redmath.categorization.dto.CategoryOverrideRequest;
import com.redmath.categorization.dto.TransactionCategoryResponse;
import com.redmath.categorization.service.CategorizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/transaction/{transactionId}/category")
@RequiredArgsConstructor
public class CategorizationController {

    private final CategorizationService categorizationService;

    @GetMapping()
    public ResponseEntity<TransactionCategoryResponse> getCategory(@PathVariable Long transactionId) {
        return ResponseEntity.ok(categorizationService.getCategory(transactionId));
    }

    /**
     * Triggers categorization for a transaction. Intended to be called by the client
     * right after a transaction is created; the same result is also produced
     * automatically via the internal TransactionCreatedEvent, so calling this is a
     * safe no-op if the transaction is already categorized.
     */
    @PostMapping()
    public ResponseEntity<TransactionCategoryResponse> categorize(@PathVariable Long transactionId) {
        return ResponseEntity.ok(categorizationService.categorizeIfNeeded(transactionId));
    }

    @PutMapping()
    public ResponseEntity<TransactionCategoryResponse> overrideCategory(
            @PathVariable Long transactionId,
            @Valid @RequestBody CategoryOverrideRequest request) {

        return ResponseEntity.ok(categorizationService.overrideCategory(transactionId, request));
    }
}
