package com.redmath.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmath.transactions.Indicator;
import com.redmath.transactions.dto.CreateTransactionRequest;
import com.redmath.account.Account;
import com.redmath.account.AccountRepository;
import com.redmath.balance.Balance;
import com.redmath.balance.repository.BalanceRepository;
import com.redmath.transactions.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
class TransactionE2ETest {


    @Autowired
    private WebApplicationContext context;


    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private BalanceRepository balanceRepository;


    @Autowired
    private TransactionRepository transactionRepository;


    private MockMvc mockMvc;


    private Account account;


    @BeforeEach
    void setup() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();


        transactionRepository.deleteAll();
        balanceRepository.deleteAll();
        accountRepository.deleteAll();


        account = new Account();

        account.setName("Test User");
        account.setEmail("test@gmail.com");
        account.setPassword("password");
        accountRepository.save(account);


        Balance balance = new Balance();

        balance.setAccount(account);
        balance.setAmount(new BigDecimal("1000"));

        balanceRepository.save(balance);
    }



    @Test
    @WithMockUser(username = "test@gmail.com")
    void shouldCreateTransactionSuccessfully() throws Exception {


        CreateTransactionRequest request =
                new CreateTransactionRequest();

        request.setAmount(new BigDecimal("500"));
        request.setDescription("Shopping");
        request.setIndicator(Indicator.DB);



        mockMvc.perform(
                        post("/api/v1/user/transaction")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description")
                        .value("Shopping"));



        Balance updatedBalance =
                balanceRepository
                        .findByAccountUserId(account.getUserId())
                        .get();


        assert updatedBalance.getAmount()
                .equals(new BigDecimal("500"));
    }



    @Test
    @WithMockUser(username = "test@gmail.com")
    void shouldGetOwnTransactions() throws Exception {


        mockMvc.perform(
                        get("/api/v1/user/transaction")
                )
                .andExpect(status().isOk());
    }



    @Test
    @WithMockUser(username = "wrong@gmail.com")
    void shouldNotFindOtherUserTransaction() throws Exception {


        mockMvc.perform(
                        get("/api/v1/user/transaction")
                )
                .andExpect(status().isNotFound());
    }



    @Test
    @WithMockUser(username = "test@gmail.com")
    void shouldFailWhenInsufficientBalance() throws Exception {


        CreateTransactionRequest request =
                new CreateTransactionRequest();


        request.setAmount(new BigDecimal("5000"));
        request.setDescription("Expensive");
        request.setIndicator(Indicator.DB);



        mockMvc.perform(
                        post("/api/v1/user/transaction")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }

}