package com.redmath.categorization.service;

import com.redmath.categorization.client.LlmClient;
import com.redmath.categorization.client.dto.LlmCategorizationRequest;
import com.redmath.categorization.client.dto.LlmCategorizationResponse;
import com.redmath.categorization.dto.CategoryOverrideRequest;
import com.redmath.categorization.dto.TransactionCategoryResponse;
import com.redmath.categorization.entity.Category;
import com.redmath.categorization.entity.CategorySource;
import com.redmath.categorization.entity.TransactionCategory;
import com.redmath.categorization.exception.InvalidCategoryOverrideException;
import com.redmath.categorization.exception.TransactionNotFoundException;
import com.redmath.categorization.repository.TransactionCategoryRepository;
import com.redmath.categorization.rule.RuleBasedCategorizer;
import com.redmath.transactions.entity.Transaction;
import com.redmath.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Orchestrates the hybrid categorization flow described in the feature spec:
 * predefined rules first, LLM fallback second, UNCATEGORIZED when neither
 * yields a confident answer. The result is persisted once so the AI is never
 * called again for the same transaction.
 */
@Service
@RequiredArgsConstructor
public class CategorizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategorizationService.class);

    private final TransactionRepository transactionRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final RuleBasedCategorizer ruleBasedCategorizer;
    private final LlmClient llmClient;

    /**
     * Categorizes a transaction if it has not already been categorized. Safe to call
     * more than once for the same transaction id - subsequent calls are no-ops.
     */
    @Transactional
    public TransactionCategoryResponse categorizeIfNeeded(Long transactionId) {

        Optional<TransactionCategory> existing = transactionCategoryRepository.findByTransactionId(transactionId);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found with id: " + transactionId));

        Category category;
        CategorySource source;
        double confidence;

        Optional<Category> ruleMatch = ruleBasedCategorizer.match(transaction.getDescription());

        if (ruleMatch.isPresent()) {
            category = ruleMatch.get();
            source = CategorySource.RULE;
            confidence = 1.0;
        } else {
            LlmCategorizationRequest llmRequest = new LlmCategorizationRequest(
                    transaction.getDescription(),
                    transaction.getAmount(),
                    transaction.getIndicator() != null ? transaction.getIndicator().name() : null,
                    transaction.getDate()
            );

            Optional<LlmCategorizationResponse> llmResponse = llmClient.categorize(llmRequest);

            if (llmResponse.isPresent()) {
                category = parseCategorySafely(llmResponse.get().category());
                source = CategorySource.LLM;
                confidence = llmResponse.get().confidence();
            } else {
                // Not enough information, or the LLM is unavailable - never force a guess.
                category = Category.UNCATEGORIZED;
                source = CategorySource.LLM;
                confidence = 0.0;
            }
        }

        TransactionCategory transactionCategory = TransactionCategory.builder()
                .transaction(transaction)
                .category(category)
                .categorySource(source)
                .confidence(confidence)
                .build();

        TransactionCategory saved = transactionCategoryRepository.save(transactionCategory);

        return toResponse(saved);
    }

    @Transactional
    public TransactionCategoryResponse overrideCategory(Long transactionId, CategoryOverrideRequest request) {

        Category category = parseCategoryStrict(request.category());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found with id: " + transactionId));

        TransactionCategory transactionCategory = transactionCategoryRepository.findByTransactionId(transactionId)
                .orElseGet(() -> TransactionCategory.builder()
                        .transaction(transaction)
                        .build());

        transactionCategory.setCategory(category);
        transactionCategory.setCategorySource(CategorySource.USER);
        transactionCategory.setConfidence(1.0);

        TransactionCategory saved = transactionCategoryRepository.save(transactionCategory);

        return toResponse(saved);
    }

    public TransactionCategoryResponse getCategory(Long transactionId) {
        TransactionCategory transactionCategory = transactionCategoryRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction has not been categorized yet: " + transactionId));

        return toResponse(transactionCategory);
    }

    private Category parseCategorySafely(String value) {
        try {
            return Category.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ex) {
            LOGGER.warn("Received an unrecognized category '{}', defaulting to UNCATEGORIZED", value);
            return Category.UNCATEGORIZED;
        }
    }

    private Category parseCategoryStrict(String value) {
        try {
            return Category.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidCategoryOverrideException(
                    "'" + value + "' is not a valid category. Allowed values: "
                            + List.of(Category.values()));
        }
    }

    private TransactionCategoryResponse toResponse(TransactionCategory transactionCategory) {
        return new TransactionCategoryResponse(
                transactionCategory.getTransaction().getId(),
                transactionCategory.getCategory(),
                transactionCategory.getCategorySource(),
                transactionCategory.getConfidence(),
                transactionCategory.getCategorizedAt()
        );
    }
}
