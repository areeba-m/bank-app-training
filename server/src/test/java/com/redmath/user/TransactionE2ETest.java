package com.redmath.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmath.account.Account;
import com.redmath.account.AccountRepository;
import com.redmath.account.Role;
import com.redmath.balance.Balance;
import com.redmath.balance.repository.BalanceRepository;
import com.redmath.transactions.Indicator;
import com.redmath.transactions.dto.CreateTransactionRequest;
import com.redmath.transactions.exception.InsufficientBalanceException;
import com.redmath.transactions.repository.TransactionRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIT
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
    private PasswordEncoder passwordEncoder;

    private Account account;

    @BeforeEach
    void setup()
    {
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
        Balance savedBalance = balanceRepository.findByAccountUserId(account.getUserId())
                        .orElseThrow();
    }

    private @NonNull RequestPostProcessor userJwt()
    {
        return jwt().jwt(jwt -> jwt
                        .subject("test@gmail.com"))
                .authorities(
                        new SimpleGrantedAuthority("ROLE_USER")
                );
    }

    @Test
    void shouldCreateCreditTransaction() throws Exception
    {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("500"));
        request.setIndicator(Indicator.CR);
        request.setDescription("Salary");

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(500));

        Balance updatedBalance = balanceRepository.findByAccountUserId(account.getUserId())
                        .orElseThrow();

        assert updatedBalance.getAmount().compareTo(new BigDecimal("1500")) == 0;
    }

    @Test
    void shouldCreateDebitTransaction() throws Exception
    {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("300"));
        request.setIndicator(Indicator.DB);
        request.setDescription("Shopping");
        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        Balance updatedBalance = balanceRepository.findByAccountUserId(account.getUserId())
                        .orElseThrow();

        assert updatedBalance.getAmount().compareTo(new BigDecimal("700")) == 0;

    }

    @Test
    void shouldFailWhenInsufficientBalance() throws Exception
    {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("5000"));
        request.setIndicator(Indicator.DB);
        request.setDescription("Large Payment");

        Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(
                post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andReturn()
                );
        assertInstanceOf(InsufficientBalanceException.class, exception.getCause());
    }

    @Test
    void shouldGetTransactions() throws Exception
    {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("200"));
        request.setIndicator(Indicator.CR);
        request.setDescription("Deposit");

        mockMvc.perform(post("/api/v1/user/transaction")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/user/transaction")
                        .with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                                .value(1));

    }

    @Test
    void shouldRejectWithoutAuthentication() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/transaction"))
                .andExpect(status().isUnauthorized());
    }

}