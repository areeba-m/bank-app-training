package com.redmath.categorization.service;

import com.redmath.categorization.client.LlmClient;
import com.redmath.categorization.dto.SpendingAnalysisResponse;
import com.redmath.categorization.entity.Category;
import com.redmath.categorization.repository.TransactionCategoryRepository;
import com.redmath.enums.Indicator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * All monetary totals here come from the database via TransactionCategoryRepository.
 * The LLM, when enabled, is only ever handed the numbers Java already computed and
 * asked to narrate them - it is never the source of truth for a total.
 */
@Service
@RequiredArgsConstructor
public class SpendingAnalysisService {

    private final TransactionCategoryRepository transactionCategoryRepository;
    private final LlmClient llmClient;

    public SpendingAnalysisResponse analyze(Long userId, Instant from, Instant to, boolean includeInsight) {

        Map<String, BigDecimal> totalsByCategory = calculateTotals(userId, from, to);
        BigDecimal totalSpending = totalsByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Double> percentageByCategory = calculatePercentages(totalsByCategory, totalSpending);

        String aiInsight = null;
        if (includeInsight) {
            aiInsight = generateInsight(userId, from, to, totalsByCategory);
        }

        return new SpendingAnalysisResponse(
                from,
                to,
                totalSpending,
                totalsByCategory,
                percentageByCategory,
                aiInsight
        );
    }

    /**
     * Convenience overload analyzing the current calendar month, with the previous
     * calendar month used for the AI insight comparison.
     */
    public SpendingAnalysisResponse analyzeCurrentMonth(Long userId, boolean includeInsight) {
        YearMonth currentMonth = YearMonth.now(ZoneOffset.UTC);
        Instant from = currentMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = currentMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return analyze(userId, from, to, includeInsight);
    }

    private Map<String, BigDecimal> calculateTotals(Long userId, Instant from, Instant to) {
        List<TransactionCategoryRepository.CategoryTotalProjection> rows =
                transactionCategoryRepository.sumAmountsByCategory(userId, Indicator.DB, from, to);

        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (TransactionCategoryRepository.CategoryTotalProjection row : rows) {
            Category category = row.getCategory();
            totals.put(category != null ? category.name() : Category.UNCATEGORIZED.name(), row.getTotal());
        }
        return totals;
    }

    private Map<String, Double> calculatePercentages(Map<String, BigDecimal> totals, BigDecimal grandTotal) {
        Map<String, Double> percentages = new LinkedHashMap<>();

        if (grandTotal.compareTo(BigDecimal.ZERO) == 0) {
            totals.keySet().forEach(category -> percentages.put(category, 0.0));
            return percentages;
        }

        totals.forEach((category, amount) -> {
            double percentage = amount
                    .divide(grandTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            percentages.put(category, percentage);
        });

        return percentages;
    }

    private String generateInsight(Long userId, Instant from, Instant to, Map<String, BigDecimal> currentTotals) {
        long spanMillis = to.toEpochMilli() - from.toEpochMilli();

        Instant previousFrom = Instant.ofEpochMilli(from.toEpochMilli() - spanMillis);

        Map<String, BigDecimal> previousTotals = calculateTotals(userId, previousFrom, from);

        Optional<String> insight = llmClient.explainSpending(
                Map.copyOf(currentTotals),
                Map.copyOf(previousTotals)
        );

        return insight.orElse(null);
    }
}
