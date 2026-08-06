package com.redmath.user;


import com.redmath.account.entity.Account;
import com.redmath.account.repository.AccountRepository;
import com.redmath.account.entity.Role;
import com.redmath.balance.entity.Balance;
import com.redmath.balance.exception.BalanceNotFoundException;
import com.redmath.balance.repository.BalanceRepository;
import com.redmath.transactions.entity.Indicator;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
@AutoConfigureMockMvc
class BalanceControllerIT
{
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    private Account account;

    @BeforeEach
    void setup()
    {
        balanceRepository.deleteAll();
        accountRepository.deleteAll();
        account = new Account();
        account.setName("Test User");
        account.setEmail("test@gmail.com");
        account.setPassword("password");
        account.setAddress("Lahore");
        account.setRole(Role.USER);
        account.setCreatedAt(Instant.now());
        account.setUpdatedAt(Instant.now());
        account = accountRepository.saveAndFlush(account);

        Balance balance = new Balance();
        balance.setAccount(account);
        balance.setAmount(new BigDecimal("5000"));
        balance.setIndicator(Indicator.CR);

        balanceRepository.saveAndFlush(balance);
    }

    private @NonNull RequestPostProcessor userJwt(String email)
    {
        return jwt().jwt(jwt -> jwt.subject(email))
                .authorities(new SimpleGrantedAuthority("ROLE_USER")
                );
    }

    @Test
    void shouldGetOwnBalance() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/balance")
                        .with(userJwt("test@gmail.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount")
                        .value(5000))
                .andExpect(jsonPath("$.indicator")
                        .value("CR"));
    }

    @Test
    void shouldFailWhenAccountDoesNotExist() throws Exception
    {
        Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(get("/api/v1/user/balance")
                                        .with(userJwt("wrong@gmail.com")))
                                        .andReturn());
        assertInstanceOf(BalanceNotFoundException.class, exception.getCause());
    }

    @Test
    void shouldReturnBalanceDetails() throws Exception
    {
        mockMvc.perform(get("/api/v1/user/balance")
                        .with(userJwt("test@gmail.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.amount").exists())
                .andExpect(jsonPath("$.indicator").exists());
    }
}