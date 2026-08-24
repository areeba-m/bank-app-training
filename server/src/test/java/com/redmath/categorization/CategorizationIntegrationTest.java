package com.redmath.categorization;

import com.redmath.account.entity.Account;
import com.redmath.account.entity.Role;
import com.redmath.account.repository.AccountRepository;
import com.redmath.account.balance.entity.Balance;
import com.redmath.account.balance.repository.BalanceRepository;
import com.redmath.categorization.entity.Category;
import com.redmath.categorization.entity.CategorySource;
import com.redmath.categorization.repository.TransactionCategoryRepository;
import com.redmath.transfer.transactions.dto.CreateTransactionRequest;
import com.redmath.enums.Indicator;
import com.redmath.transfer.transactions.repository.TransactionRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Account account;

    private String idempotencyKey;

    @BeforeEach
    void setup() {
        transactionCategoryRepository.deleteAll();
        transactionRepository.deleteAll();
        balanceRepository.deleteAll();
        accountRepository.deleteAll();

        account = new Account();
        account.setName("Test User");
        account.setEmail("categorization-test@gmail.com");
        account.setPassword(passwordEncoder.encode("password"));
        account.setAddress("Lahore");
        account.setRole(Role.USER);
        account.setCreatedAt(Instant.now());
        account.setUpdatedAt(Instant.now());
        account = accountRepository.saveAndFlush(account);

        Balance balance = new Balance();
//        balance.setAccount(account);
        balance.setAmount(new BigDecimal("10000"));
        balance.setIndicator(Indicator.CR);
        balanceRepository.saveAndFlush(balance);

        idempotencyKey = UUID.randomUUID().toString();
    }

    private @NonNull RequestPostProcessor userJwt() {
        return jwt().jwt(jwt -> jwt
                        .subject("categorization-test@gmail.com")
                        .claim("userId", account.getUserId()))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    void shouldAutoCategorizeTransactionUsingRuleOnCreation() throws Exception {

        CreateTransactionRequest request = new CreateTransactionRequest(
                "NETFLIX SUBSCRIPTION",
                new BigDecimal("1500"),
                Indicator.DB
        );

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk());

        var transaction = transactionRepository.findByAccountUserId(
                        account.getUserId(), PageRequest.of(0, 10))
                .getContent().getFirst();
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var category = transactionCategoryRepository.findByTransactionId(transaction.getId()).orElseThrow();

            assertEquals(Category.ENTERTAINMENT, category.getCategory());
            assertEquals(CategorySource.RULE, category.getCategorySource());

            mockMvc.perform(get("/api/v1/user/transaction/" + transaction.getId() + "/category")
                            .with(userJwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.category").value("ENTERTAINMENT"))
                    .andExpect(jsonPath("$.categorySource").value("RULE"));
        });
    }

    @Test
    void shouldDefaultToUncategorizedForMeaninglessDescriptionWhenLlmDisabled() throws Exception {

        CreateTransactionRequest request = new CreateTransactionRequest(
                "TRX-839201",
                new BigDecimal("2500"),
                Indicator.DB
        );

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk());

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var page = transactionRepository.findByAccountUserId(
                    account.getUserId(), PageRequest.of(0, 10));

            assertFalse(page.getContent().isEmpty());
            var transaction = page.getContent().getFirst();

            var categoryOpt = transactionCategoryRepository.findByTransactionId(transaction.getId());
            assertTrue(categoryOpt.isPresent());
            assertEquals(Category.UNCATEGORIZED, categoryOpt.get().getCategory());
        });
    }

    @Test
    void shouldAllowUserToOverrideCategory() throws Exception {

        idempotencyKey = UUID.randomUUID().toString();

        CreateTransactionRequest request = new CreateTransactionRequest(
                "Unusual one-off purchase",
                new BigDecimal("400"),
                Indicator.DB
        );

        String response = mockMvc.perform(
                        post("/api/v1/user/transaction")
                                .with(userJwt())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Idempotency-Key", idempotencyKey)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long transactionId = objectMapper
                .readTree(response)
                .path("id")
                .asLong();

        mockMvc.perform(
                        put(
                                "/api/v1/user/transaction/{transactionId}/category",
                                transactionId
                        )
                                .with(userJwt())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "category": "HEALTH"
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("HEALTH"))
                .andExpect(jsonPath("$.categorySource").value("USER"));

        var category = transactionCategoryRepository
                .findByTransactionId(transactionId)
                .orElseThrow();

        assertEquals(Category.HEALTH, category.getCategory());
        assertEquals(CategorySource.USER, category.getCategorySource());
    }

    @Test
    void shouldReturnSpendingAnalysisGroupedByCategory() throws Exception {

        idempotencyKey = UUID.randomUUID().toString();
        createDebitTransaction("NETFLIX", "1500");
        createDebitTransaction("MCDONALD'S", "500");

        mockMvc.perform(get("/api/v1/user/analytics/spending")
                        .with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byCategory.ENTERTAINMENT").value(1500))
                .andExpect(jsonPath("$.byCategory.FOOD").value(500))
                .andExpect(jsonPath("$.totalSpending").value(2000))
                .andExpect(jsonPath("$.percentageByCategory.FOOD").value(25.0));
    }

    @Test
    void shouldRejectAnalyticsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/user/analytics/spending"))
                .andExpect(status().isUnauthorized());
    }

    private void createDebitTransaction(String description, String amount) throws Exception {
        idempotencyKey = UUID.randomUUID().toString();
        CreateTransactionRequest request = new CreateTransactionRequest(
                description,
                new BigDecimal(amount),
                Indicator.DB
        );

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk());
    }
}

