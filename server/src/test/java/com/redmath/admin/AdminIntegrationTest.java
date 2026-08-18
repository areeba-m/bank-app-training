package com.redmath.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmath.ServerApplication;
import com.redmath.account.entity.Account;
import com.redmath.account.entity.Role;
import com.redmath.account.repository.AccountRepository;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UpdateUserRequest;
import com.redmath.authentication.entity.OtpToken;
import com.redmath.authentication.repository.OtpTokenRepository;
import com.redmath.authentication.service.EmailService;
import com.redmath.transactions.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ServerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @MockitoBean
    private EmailService emailService;


    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        otpTokenRepository.deleteAll();
        accountRepository.deleteAll();
    }


    @Test
    void adminCrudFlowShouldWork() throws Exception {

        // ---------- CREATE ----------

        CreateUserRequest createRequest = new CreateUserRequest(
                "Alice",
                "alice@example.com",
                "Lahore"
        );

        mockMvc.perform(post("/api/v1/admin/accounts")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.address").value("Lahore"))
                .andExpect(jsonPath("$.role").value("USER"));

        Account saved = accountRepository.findByEmail("alice@example.com").orElseThrow();

        assertEquals("Alice", saved.getName());
        assertEquals(Role.USER, saved.getRole());

        assertTrue(saved.isPasswordChangeRequired());
        assertNotNull(saved.getPassword());

        verify(emailService, timeout(2000)).sendOtpEmail(
                eq("alice@example.com"),
                eq("Alice"),
                anyString(),
                anyLong());

        OtpToken issuedToken = otpTokenRepository
                .findTopByAccountAndUsedFalseOrderByCreatedAtDesc(saved)
                .orElseThrow(() -> new AssertionError("Expected an OTP token to be issued on account creation"));
        assertFalse(issuedToken.isUsed());
        assertTrue(issuedToken.getExpiresAt().isAfter(java.time.Instant.now()));

        // ---------- GET BY ID ----------

        mockMvc.perform(get("/api/v1/admin/accounts/{id}", saved.getUserId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getUserId()))
                .andExpect(jsonPath("$.name").value("Alice"));

        // ---------- UPDATE ----------

        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Alice Updated",
                "Islamabad"
        );

        mockMvc.perform(patch("/api/v1/admin/accounts/{id}", saved.getUserId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"))
                .andExpect(jsonPath("$.address").value("Islamabad"));

        Account updated =
                accountRepository.findById(saved.getUserId()).orElseThrow();

        assertEquals("Alice Updated", updated.getName());
        assertEquals("Islamabad", updated.getAddress());

        // ---------- GET ALL ----------

        mockMvc.perform(get("/api/v1/admin/accounts")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        // ---------- DELETE ----------

        mockMvc.perform(delete("/api/v1/admin/accounts/{id}", saved.getUserId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertFalse(accountRepository.existsById(saved.getUserId()));
    }
}