package com.redmath.categorization.client.dto;

public record LlmCategorizationResponse(
        String category,
        double confidence
) {
}
