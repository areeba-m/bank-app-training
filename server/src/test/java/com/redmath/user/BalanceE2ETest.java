package com.redmath.user;

import com.redmath.account.Account;
import com.redmath.account.AccountRepository;
import com.redmath.balance.Balance;
import com.redmath.balance.repository.BalanceRepository;
import com.redmath.transactions.Indicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@SpringBootTest
class BalanceE2ETest {


    @Autowired
    private WebApplicationContext context;


    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private BalanceRepository balanceRepository;


    private MockMvc mockMvc;


    private Account account;



    @BeforeEach
    void setup() {


        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();


        balanceRepository.deleteAll();
        accountRepository.deleteAll();


        account = new Account();

        account.setName("Test User");
        account.setEmail("test@gmail.com");
        account.setPassword("password");

        accountRepository.save(account);



        Balance balance = new Balance();

        balance.setAccount(account);
        balance.setAmount(new BigDecimal("5000"));
        balance.setIndicator(Indicator.CR);

        balanceRepository.save(balance);
    }



    @Test
    @WithMockUser(username = "test@gmail.com")
    void shouldGetOwnBalance() throws Exception {


        mockMvc.perform(
                        get("/api/v1/user/balance")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount")
                        .value(5000));
    }



    @Test
    @WithMockUser(username = "wrong@gmail.com")
    void shouldNotGetAnotherUserBalance() throws Exception {


        mockMvc.perform(
                        get("/api/v1/user/balance")
                )
                .andExpect(status().isNotFound());
    }



    @Test
    @WithMockUser(username = "test@gmail.com")
    void shouldReturnBalanceDetails() throws Exception {


        mockMvc.perform(
                        get("/api/v1/user/balance")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indicator")
                        .exists())
                .andExpect(jsonPath("$.date")
                        .exists());
    }



    @Test
    @WithMockUser(username = "test@gmail.com")
    void shouldFailWhenBalanceDoesNotExist() throws Exception {


        balanceRepository.deleteAll();


        mockMvc.perform(
                        get("/api/v1/user/balance")
                )
                .andExpect(status().isNotFound());
    }

}