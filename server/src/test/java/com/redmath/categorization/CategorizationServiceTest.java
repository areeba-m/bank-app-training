package com.redmath.categorization;

import com.redmath.account.entity.Account;
import com.redmath.categorization.client.LlmClient;
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
import com.redmath.categorization.service.CategorizationService;
import com.redmath.transactions.entity.Indicator;
import com.redmath.transactions.entity.Transaction;
import com.redmath.transactions.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategorizationServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;

    @Mock
    private LlmClient llmClient;

    private CategorizationService categorizationService;

    @BeforeEach
    void setup() {
        categorizationService = new CategorizationService(
                transactionRepository,
                transactionCategoryRepository,
                new RuleBasedCategorizer(),
                llmClient
        );
    }

    private Transaction transactionWithDescription(String description) {
        Account account = new Account();
        account.setUserId(1L);

        Transaction transaction = new Transaction();
        transaction.setId(42L);
        transaction.setDescription(description);
        transaction.setAmount(new BigDecimal("2500"));
        transaction.setIndicator(Indicator.DB);
        transaction.setDate(Instant.now());
        transaction.setAccount(account);
        return transaction;
    }

    @Test
    void shouldCategorizeUsingRuleWithoutCallingLlm() {
        Transaction transaction = transactionWithDescription("NETFLIX SUBSCRIPTION");

        when(transactionCategoryRepository.findByTransactionId(42L)).thenReturn(Optional.empty());
        when(transactionRepository.findById(42L)).thenReturn(Optional.of(transaction));
        when(transactionCategoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionCategoryResponse response = categorizationService.categorizeIfNeeded(42L);

        assertEquals(Category.ENTERTAINMENT, response.category());
        assertEquals(CategorySource.RULE, response.categorySource());
        verify(llmClient, never()).categorize(any());
    }

    @Test
    void shouldFallBackToLlmWhenNoRuleMatches() {
        Transaction transaction =
                transactionWithDescription("UNKNOWN_MERCHANT_XYZ_123");

        when(transactionCategoryRepository.findByTransactionId(42L))
                .thenReturn(Optional.empty());

        when(transactionRepository.findById(42L))
                .thenReturn(Optional.of(transaction));

        when(llmClient.categorize(any()))
                .thenReturn(Optional.of(
                        new LlmCategorizationResponse("FOOD", 0.95)
                ));

        when(transactionCategoryRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionCategoryResponse response =
                categorizationService.categorizeIfNeeded(42L);

        assertEquals(Category.FOOD, response.category());
        assertEquals(CategorySource.LLM, response.categorySource());
        assertEquals(0.95, response.confidence());

        verify(llmClient).categorize(any());
    }

    @Test
    void shouldDefaultToUncategorizedWhenLlmUnavailable() {
        Transaction transaction = transactionWithDescription("TRX-839201");

        when(transactionCategoryRepository.findByTransactionId(42L)).thenReturn(Optional.empty());
        when(transactionRepository.findById(42L)).thenReturn(Optional.of(transaction));
        when(llmClient.categorize(any())).thenReturn(Optional.empty());
        when(transactionCategoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionCategoryResponse response = categorizationService.categorizeIfNeeded(42L);

        assertEquals(Category.UNCATEGORIZED, response.category());
        assertEquals(CategorySource.LLM, response.categorySource());
    }

    @Test
    void shouldNotRecategorizeAlreadyCategorizedTransaction() {
        Transaction transaction = transactionWithDescription("NETFLIX");
        TransactionCategory existing = TransactionCategory.builder()
                .transaction(transaction)
                .category(Category.ENTERTAINMENT)
                .categorySource(CategorySource.RULE)
                .confidence(1.0)
                .categorizedAt(Instant.now())
                .build();

        when(transactionCategoryRepository.findByTransactionId(42L)).thenReturn(Optional.of(existing));

        TransactionCategoryResponse response = categorizationService.categorizeIfNeeded(42L);

        assertEquals(Category.ENTERTAINMENT, response.category());
        verify(transactionRepository, never()).findById(anyLong());
        verify(transactionCategoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTransactionMissing() {
        when(transactionCategoryRepository.findByTransactionId(99L)).thenReturn(Optional.empty());
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> categorizationService.categorizeIfNeeded(99L));
    }

    @Test
    void shouldOverrideCategoryWithUserSource() {
        Transaction transaction = transactionWithDescription("Unusual purchase");

        when(transactionRepository.findById(42L)).thenReturn(Optional.of(transaction));
        when(transactionCategoryRepository.findByTransactionId(42L)).thenReturn(Optional.empty());

        ArgumentCaptor<TransactionCategory> captor = ArgumentCaptor.forClass(TransactionCategory.class);
        when(transactionCategoryRepository.save(captor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionCategoryResponse response = categorizationService.overrideCategory(
                42L, new CategoryOverrideRequest("SHOPPING"));

        assertEquals(Category.SHOPPING, response.category());
        assertEquals(CategorySource.USER, response.categorySource());
        assertEquals(Category.SHOPPING, captor.getValue().getCategory());
    }

    @Test
    void shouldRejectInvalidOverrideCategory() {
        assertThrows(InvalidCategoryOverrideException.class,
                () -> categorizationService.overrideCategory(42L, new CategoryOverrideRequest("NOT_REAL")));
    }
}
