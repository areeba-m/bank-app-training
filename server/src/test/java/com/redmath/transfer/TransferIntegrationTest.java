package com.redmath.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmath.account.entity.Account;
import com.redmath.account.entity.Role;
import com.redmath.account.repository.AccountRepository;
import com.redmath.account.balance.entity.Balance;
import com.redmath.account.balance.repository.BalanceRepository;
import com.redmath.categorization.repository.TransactionCategoryRepository;
import com.redmath.enums.Indicator;
import com.redmath.transfer.transactions.repository.TransactionRepository;
import com.redmath.transfer.dto.CreateTransferRequest;
import com.redmath.transfer.repository.TransferRepository;
import com.redmath.transfer.service.TransferService;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
class TransferIntegrationTest {

    @TestConfiguration
    static class JacksonConfig {
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
    private TransferRepository transferRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionCategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Account sender;
    private Account receiver;

    @Autowired
    private TransferService transferService;

    private String idempotencyKey;

    @BeforeEach
    void setup() {
        categoryRepository.deleteAll();
        transactionRepository.deleteAll();
        transferRepository.deleteAll();
        balanceRepository.deleteAll();
        accountRepository.deleteAll();

        sender = createAccount("sender@gmail.com", "Sender");
        receiver = createAccount("receiver@gmail.com", "Receiver");

        createBalance(sender, new BigDecimal("1000"));
        createBalance(receiver, new BigDecimal("200"));

        idempotencyKey = UUID.randomUUID().toString();
    }

    private Account createAccount(String email, String name) {
        Account account = new Account();
        account.setName(name);
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode("password"));
        account.setAddress("Lahore");
        account.setRole(Role.USER);
        account.setCreatedAt(Instant.now());
        account.setUpdatedAt(Instant.now());
        return accountRepository.saveAndFlush(account);
    }

    private void createBalance(Account account, BigDecimal amount) {
        Balance balance = new Balance();
        balance.setAccount(account);
        balance.setAmount(amount);
        balance.setIndicator(Indicator.CR);
        balanceRepository.saveAndFlush(balance);
    }

    private @NonNull RequestPostProcessor jwtFor(String email) {
        return jwt().jwt(jwt -> jwt
                        .subject(email))
                .authorities(
                        new SimpleGrantedAuthority("ROLE_USER")
                );
    }

    @Test
    void shouldTransferMoneyBetweenAccounts() throws Exception {

        CreateTransferRequest request = new CreateTransferRequest(
                "receiver@gmail.com",
                new BigDecimal("300"),
                "Rent"
        );

        mockMvc.perform(post("/api/v1/user/transfer")
                        .with(jwtFor("sender@gmail.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(300))
                .andExpect(jsonPath("$.description").value("Rent"))
                .andExpect(jsonPath("$.senderEmail").value("sender@gmail.com"))
                .andExpect(jsonPath("$.recipientEmail").value("receiver@gmail.com"));

        Balance senderBalance = balanceRepository.findByAccountUserId(sender.getUserId()).orElseThrow();
        Balance receiverBalance = balanceRepository.findByAccountUserId(receiver.getUserId()).orElseThrow();

        assertEquals(0, senderBalance.getAmount().compareTo(new BigDecimal("700")));
        assertEquals(0, receiverBalance.getAmount().compareTo(new BigDecimal("500")));

        assertEquals(1, transferRepository.count());

        // Both parties should see a matching transaction entry
        assertEquals(1, transactionRepository.findByAccountUserId(
                sender.getUserId(), PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, transactionRepository.findByAccountUserId(
                receiver.getUserId(), PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void shouldFailWhenInsufficientBalance() throws Exception {

        CreateTransferRequest request = new CreateTransferRequest(
                "receiver@gmail.com",
                new BigDecimal("5000"),
                "Too much"
        );

        mockMvc.perform(post("/api/v1/user/transfer")
                        .with(jwtFor("sender@gmail.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").exists());

        Balance senderBalance = balanceRepository.findByAccountUserId(sender.getUserId()).orElseThrow();
        assertEquals(0, senderBalance.getAmount().compareTo(new BigDecimal("1000")));
        assertEquals(0, transferRepository.count());
    }

    @Test
    void shouldFailWhenRecipientDoesNotExist() throws Exception {

        CreateTransferRequest request = new CreateTransferRequest(
                "nobody@gmail.com",
                new BigDecimal("100"),
                "Ghost"
        );

        mockMvc.perform(post("/api/v1/user/transfer")
                        .with(jwtFor("sender@gmail.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldFailWhenTransferringToSelf() throws Exception {

        CreateTransferRequest request = new CreateTransferRequest(
                "sender@gmail.com",
                new BigDecimal("100"),
                "To myself"
        );

        mockMvc.perform(post("/api/v1/user/transfer")
                        .with(jwtFor("sender@gmail.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Description(value = "Test case swaps sender and receiver ")
    void shouldGetTransferHistoryAndUseBalanceConcurrency() throws Exception {

        CreateTransferRequest request = new CreateTransferRequest(
                "sender@gmail.com",
                new BigDecimal("150"),
                "Lunch"
        );

        transferService.createTransfer(request, "receiver@gmail.com", idempotencyKey);

        mockMvc.perform(get("/api/v1/user/transfer")
                        .param("page", "0")
                        .param("size", "10")
                        .with(jwtFor("receiver@gmail.com"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(150))
                .andExpect(jsonPath("$.content[0].description").value("Lunch"))
                .andExpect(jsonPath("$.content[0].date").exists());

    }

    @Test
    void shouldRejectWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/user/transfer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotCreateDuplicateTransferWithSameIdempotencyKey() throws Exception {

        CreateTransferRequest request = new CreateTransferRequest(
                "receiver@gmail.com",
                new BigDecimal("300"),
                "Rent"
        );

        // First request
        mockMvc.perform(post("/api/v1/user/transfer")
                        .with(jwtFor("sender@gmail.com"))
                        .with(csrf())
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Retry with SAME key
        mockMvc.perform(post("/api/v1/user/transfer")
                        .with(jwtFor("sender@gmail.com"))
                        .with(csrf())
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Balance senderBalance = balanceRepository.findByAccountUserId(sender.getUserId()).orElseThrow();
        Balance receiverBalance = balanceRepository.findByAccountUserId(receiver.getUserId()).orElseThrow();

        assertEquals(0, senderBalance.getAmount().compareTo(new BigDecimal("700")));
        assertEquals(0, receiverBalance.getAmount().compareTo(new BigDecimal("500")));
        assertEquals(1, transferRepository.count());
        assertEquals(1, transactionRepository.findByAccountUserId(
                sender.getUserId(),
                PageRequest.of(0, 10)).getTotalElements()
        );
        assertEquals(1, transactionRepository.findByAccountUserId(
                receiver.getUserId(),
                PageRequest.of(0, 10)).getTotalElements()
        );
    }

    @Test
    void shouldCreateSeparateTransfersWithDifferentIdempotencyKeys() throws Exception {
        String idempotencyKey2 = UUID.randomUUID().toString();

        CreateTransferRequest request = new CreateTransferRequest(
                "receiver@gmail.com",
                new BigDecimal("100"),
                "Payment"
        );

        mockMvc.perform(post("/api/v1/user/transfer")
                        .with(jwtFor("sender@gmail.com"))
                        .with(csrf())
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/user/transfer")
                        .with(jwtFor("sender@gmail.com"))
                        .with(csrf())
                        .header("Idempotency-Key", idempotencyKey2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertEquals(2, transferRepository.count());
        Balance senderBalance = balanceRepository.findByAccountUserId(sender.getUserId()).orElseThrow();
        assertEquals(0, senderBalance.getAmount().compareTo(new BigDecimal("800")));
    }

    @Test
    void shouldRejectTransferWithoutIdempotencyKey() throws Exception {

        CreateTransferRequest request = new CreateTransferRequest(
                "receiver@gmail.com",
                new BigDecimal("100"),
                "Payment"
        );

        mockMvc.perform(post("/api/v1/user/transfer")
                        .with(jwtFor("sender@gmail.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
