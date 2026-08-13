package com.redmath.categorization.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record SpendingAnalysisResponse(
        Instant from,
        Instant to,
        BigDecimal totalSpending,
        Map<String, BigDecimal> byCategory,
        Map<String, Double> percentageByCategory,
        String aiInsight
) {
}
