package com.redmath.categorization.dto;

import com.redmath.categorization.entity.Category;
import com.redmath.categorization.entity.CategorySource;

import java.time.Instant;

public record TransactionCategoryResponse(
        Long transactionId,
        Category category,
        CategorySource categorySource,
        Double confidence,
        Instant categorizedAt
) {
}
