package com.redmath.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmath.account.entity.Account;
import com.redmath.account.entity.Role;
import com.redmath.account.repository.AccountRepository;
import com.redmath.balance.entity.Balance;
import com.redmath.balance.repository.BalanceRepository;
import com.redmath.categorization.repository.TransactionCategoryRepository;
import com.redmath.transactions.dto.CreateTransactionRequest;
import com.redmath.transactions.entity.Indicator;
import com.redmath.transactions.entity.Transaction;
import com.redmath.transactions.repository.TransactionRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionIntegrationTest
{
    @TestConfiguration
    static class JacksonConfig
    {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

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
    private TransactionCategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Account account;

    private String idempotencyKey;

    @BeforeEach
    void setup()
    {
        categoryRepository.deleteAll();
        transactionRepository.deleteAll();
        balanceRepository.deleteAll();
        accountRepository.deleteAll();

        account = new Account();
        account.setName("Test User");
        account.setEmail("test@gmail.com");
        account.setPassword(passwordEncoder.encode("password"));
        account.setAddress("Lahore");
        account.setRole(Role.USER);
        account.setCreatedAt(Instant.now());
        account.setUpdatedAt(Instant.now());

        account = accountRepository.saveAndFlush(account);

        Balance balance = new Balance();
        balance.setAccount(account);
        balance.setAmount(new BigDecimal("1000"));
        balance.setIndicator(Indicator.CR);
        balanceRepository.saveAndFlush(balance);

        idempotencyKey = UUID.randomUUID().toString();
    }

    private @NonNull RequestPostProcessor userJwt()
    {
        return jwt().jwt(jwt -> jwt
                        .subject("test@gmail.com")
                        .claim("userId", account.getUserId()))
                .authorities(
                        new SimpleGrantedAuthority("ROLE_USER")
                );
    }




    @Test
    void shouldCreateCreditTransaction() throws Exception {

        CreateTransactionRequest request = new CreateTransactionRequest(
                "Salary",
                new BigDecimal("500"),
                Indicator.CR
        );

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(500))
                .andExpect(jsonPath("$.description").value("Salary"))
                .andExpect(jsonPath("$.indicator").value("CR"));

        // Verify balance updated
        Balance updatedBalance = balanceRepository
                .findByAccountUserId(account.getUserId())
                .orElseThrow();

        assertEquals(0,
                updatedBalance.getAmount().compareTo(new BigDecimal("1500")));

        // Verify transaction persisted
        Page<Transaction> transactions = transactionRepository.findByAccountUserId(
                account.getUserId(),
                PageRequest.of(0, 10));

        assertEquals(1, transactions.getTotalElements());

        Transaction transaction = transactions.getContent().getFirst();

        assertEquals("Salary", transaction.getDescription());
        assertEquals(Indicator.CR, transaction.getIndicator());
        assertEquals(0,
                transaction.getAmount().compareTo(new BigDecimal("500")));
    }


    @Test
    void shouldCreateDebitTransaction() throws Exception {

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("300"));
        request.setIndicator(Indicator.DB);
        request.setDescription("Shopping");

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(300))
                .andExpect(jsonPath("$.description").value("Shopping"))
                .andExpect(jsonPath("$.indicator").value("DB"));

        Balance updatedBalance = balanceRepository
                .findByAccountUserId(account.getUserId())
                .orElseThrow();

        assertEquals(0,
                updatedBalance.getAmount().compareTo(new BigDecimal("700")));

        // Verify the transaction was saved in the database
        Page<Transaction> transactions = transactionRepository.findByAccountUserId(
                account.getUserId(),
                PageRequest.of(0, 10));

        assertEquals(1, transactions.getTotalElements());

        Transaction transaction = transactions.getContent().getFirst();

        assertEquals("Shopping", transaction.getDescription());
        assertEquals(Indicator.DB, transaction.getIndicator());
        assertEquals(0,
                transaction.getAmount().compareTo(new BigDecimal("300")));
    }

    @Test
    void shouldFailWhenInsufficientBalance() throws Exception
    {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("5000"));
        request.setIndicator(Indicator.DB);
        request.setDescription("Large Payment");

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").exists());

        Balance balance = balanceRepository
                .findByAccountUserId(account.getUserId())
                .orElseThrow();

        assertEquals(0,
                balance.getAmount().compareTo(new BigDecimal("1000")));

        Page<Transaction> transactions = transactionRepository.findByAccountUserId(
                account.getUserId(),
                PageRequest.of(0, 10));

        assertEquals(0, transactions.getTotalElements());
    }

    @Test
    void shouldGetTransactions() throws Exception {

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("200"));
        request.setIndicator(Indicator.CR);
        request.setDescription("Deposit");

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/user/transaction")
                        .param("page", "0")
                        .param("size", "10")
                        .with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(200))
                .andExpect(jsonPath("$.content[0].description").value("Deposit"))
                .andExpect(jsonPath("$.content[0].indicator").value("CR"))
                .andExpect(jsonPath("$.content[0].date").exists());

        // Verify data directly from the database
        Page<Transaction> transactions = transactionRepository.findByAccountUserId(
                account.getUserId(),
                PageRequest.of(0, 10));

        assertEquals(1, transactions.getTotalElements());

        Transaction transaction = transactions.getContent().getFirst();

        assertEquals("Deposit", transaction.getDescription());
        assertEquals(Indicator.CR, transaction.getIndicator());
        assertEquals(0,
                transaction.getAmount().compareTo(new BigDecimal("200")));
    }

    @Test
    void shouldRejectWithoutAuthentication() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/transaction"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotCreateDuplicateTransferWithSameIdempotencyKey() throws Exception {

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("200"));
        request.setIndicator(Indicator.CR);
        request.setDescription("Deposit");

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk());

        // Retry with SAME key
        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk());

        Balance senderBalance = balanceRepository.findByAccountUserId(account.getUserId()).orElseThrow();

        assertEquals(0, senderBalance.getAmount().compareTo(new BigDecimal("1200")));
        assertEquals(1, transactionRepository.count());
    }

    @Test
    void shouldCreateSeparateTransfersWithDifferentIdempotencyKeys() throws Exception {
        String idempotencyKey2 = UUID.randomUUID().toString();

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("200"));
        request.setIndicator(Indicator.DB);
        request.setDescription("Withdrawal");

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey2))
                .andExpect(status().isOk());

        assertEquals(2, transactionRepository.count());
        Balance senderBalance = balanceRepository.findByAccountUserId(account.getUserId()).orElseThrow();
        assertEquals(0, senderBalance.getAmount().compareTo(new BigDecimal("600")));
    }

    @Test
    void shouldRejectTransferWithoutIdempotencyKey() throws Exception {

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("200"));
        request.setIndicator(Indicator.DB);
        request.setDescription("Withdrawal");

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
