package com.redmath.categorization.client.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LlmCategorizationRequest(
        String description,
        BigDecimal amount,
        String type,
        Instant date
) {
}
