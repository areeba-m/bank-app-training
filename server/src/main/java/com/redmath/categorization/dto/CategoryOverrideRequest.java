package com.redmath.categorization.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryOverrideRequest(
        @NotNull(message = "Category is required")
        String category
) {
}
