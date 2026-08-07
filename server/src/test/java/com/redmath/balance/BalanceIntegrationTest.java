package com.redmath.balance;

import com.redmath.account.entity.Account;
import com.redmath.account.entity.Role;
import com.redmath.account.repository.AccountRepository;
import com.redmath.balance.entity.Balance;
import com.redmath.balance.repository.BalanceRepository;
import com.redmath.transactions.entity.Indicator;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BalanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Account account;

    @BeforeEach
    void setup() {

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
    }

    private @NonNull RequestPostProcessor userJwt() {
        return jwt()
                .jwt(jwt -> jwt.subject("test@gmail.com"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    void shouldGetBalance() throws Exception {

        mockMvc.perform(get("/api/v1/user/balance")
                        .with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.indicator").value("CR"))
                .andExpect(jsonPath("$.date").exists());

        Balance balance = balanceRepository
                .findByAccountUserId(account.getUserId())
                .orElseThrow();

        assertEquals(0,
                balance.getAmount().compareTo(new BigDecimal("1000")));

        assertEquals(Indicator.CR, balance.getIndicator());
    }

    @Test
    void shouldRejectWithoutAuthentication() throws Exception
    {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/transaction"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBalanceFromRepository() {

        Balance balance = balanceRepository
                .findByAccountUserId(account.getUserId())
                .orElseThrow();

        assertEquals(0,
                balance.getAmount().compareTo(new BigDecimal("1000")));

        assertEquals(Indicator.CR, balance.getIndicator());

        assertNotNull(balance.getDate());

        assertEquals(account.getUserId(),
                balance.getAccount().getUserId());
    }

    @Test
    void shouldReturnCorrectBalanceResponse() throws Exception {

        mockMvc.perform(get("/api/v1/user/balance")
                        .with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.indicator").value("CR"))
                .andExpect(jsonPath("$.date").exists());

        Balance balance = balanceRepository
                .findByAccountUserId(account.getUserId())
                .orElseThrow();

        assertNotNull(balance);

        assertEquals(Indicator.CR,
                balance.getIndicator());
    }
}
