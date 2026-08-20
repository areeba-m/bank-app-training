package com.redmath.authentication;

import com.redmath.account.entity.Account;
import com.redmath.account.entity.Role;
import com.redmath.account.repository.AccountRepository;
import com.redmath.authentication.dto.LoginRequest;
import com.redmath.authentication.dto.RegisterRequest;
import com.redmath.authentication.dto.ResendOtpRequest;
import com.redmath.authentication.dto.SetNewPasswordRequest;
import com.redmath.authentication.entity.OtpToken;
import com.redmath.authentication.entity.RefreshToken;
import com.redmath.authentication.repository.OtpTokenRepository;
import com.redmath.authentication.repository.RefreshTokenRepository;
import com.redmath.authentication.service.EmailService;
import com.redmath.authentication.service.RefreshTokenService;
import com.redmath.authentication.wrapper.AccountPrincipal;
import com.redmath.categorization.repository.TransactionCategoryRepository;
import com.redmath.transactions.entity.Indicator;
import com.redmath.transactions.repository.TransactionRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    private static final String BASE = "/api/v1/auth";

    private static final String EMAIL = "jane.doe@example.com";
    private static final String PASSWORD = "S3curePassw0rd!";
    private static final String NEW_PASSWORD = "NewS3curePassw0rd!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionCategoryRepository categoryRepository;

    private static final String EMAIL = "jane.doe@example.com";
    private static final String PASSWORD = "S3curePassw0rd!";
    @Autowired
    private PasswordEncoder passwordEncoder;

    //@MockBean
    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void cleanDb() {
        categoryRepository.deleteAll();
        transactionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        otpTokenRepository.deleteAll();
        accountRepository.deleteAll();

        reset(emailService);

        doNothing().when(emailService).sendOtpEmail(
                anyString(),
                anyString(),
                anyString(),
                anyLong()
        );
    }

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        otpTokenRepository.deleteAll();
        accountRepository.deleteAll();
    }

    // ===============================================================
    // Registration
    // ===============================================================

    @Test
    void register_withNewEmail_createsAccountAndInitialBalance()
            throws Exception {

        RegisterRequest request = new RegisterRequest(
                "Jane Doe",
                EMAIL,
                PASSWORD,
                "123 Main St"
        );

        mockMvc.perform(post(BASE + "/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value(Role.USER.name()));

        Account account = accountRepository
                .findByEmail(EMAIL)
                .orElseThrow();

        assertThat(account.getRole()).isEqualTo(Role.USER);

        assertThat(account.getPassword())
                .isNotEqualTo(PASSWORD);

        assertThat(passwordEncoder.matches(
                PASSWORD,
                account.getPassword()
        )).isTrue();

        assertThat(account.getBalance()).isNotNull();

        assertThat(account.getBalance().getAmount())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(account.getBalance().getIndicator())
                .isEqualTo(Indicator.CR);

        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getUpdatedAt()).isNotNull();
    }

    @Test
    void register_withDuplicateEmail_returns4xx()
            throws Exception {

        registerUser();

        RegisterRequest request = new RegisterRequest(
                "Another User",
                EMAIL,
                "AnotherPassword123!",
                "Another Address"
        );

        mockMvc.perform(post(BASE + "/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        assertThat(accountRepository.count()).isEqualTo(1);
    }

    @Test
    void register_withInvalidPayload_returns400()
            throws Exception {

        String request = """
                {
                    "name": "",
                    "email": "invalid-email",
                    "password": "",
                    "address": ""
                }
                """;

        mockMvc.perform(post(BASE + "/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(status().isBadRequest());

        assertThat(accountRepository.count()).isZero();
    }

    // ===============================================================
    // Login
    // ===============================================================

    @Test
    void login_withValidCredentials_returnsTokens()
            throws Exception {

        registerUser();

        LoginRequest request = new LoginRequest(
                EMAIL,
                PASSWORD
        );

        MvcResult result = mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value(Role.USER.name()))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("refresh_token", true))
                .andReturn();

        Cookie refreshCookie = result
                .getResponse()
                .getCookie("refresh_token");

        assertThat(refreshCookie).isNotNull();

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(
                        Objects.requireNonNull(refreshCookie).getValue()
                )
                .orElseThrow();

        assertThat(refreshToken.getUser().getEmail())
                .isEqualTo(EMAIL);

        assertThat(refreshToken.getExpiryDate())
                .isAfter(Instant.now());
    }

    @Test
    void login_withWrongPassword_returns401()
            throws Exception {

        registerUser();

        LoginRequest request = new LoginRequest(
                EMAIL,
                "WrongPassword123!"
        );

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        assertThat(refreshTokenRepository.count()).isZero();
    }

    @Test
    void login_withUnknownEmail_returns401()
            throws Exception {

        LoginRequest request = new LoginRequest(
                "unknown@example.com",
                PASSWORD
        );

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        assertThat(refreshTokenRepository.count()).isZero();
    }

    // ===============================================================
    // Refresh Token
    // ===============================================================

    @Test
    void refresh_withValidToken_rotatesRefreshToken()
            throws Exception {

        registerUser();

        Cookie oldCookie = getRefreshCookie();

        String oldToken =
                Objects.requireNonNull(oldCookie).getValue();

        MvcResult result = mockMvc.perform(post(BASE + "/refresh")
                        .with(csrf())
                        .cookie(oldCookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        Cookie newCookie = result
                .getResponse()
                .getCookie("refresh_token");

        assertThat(newCookie).isNotNull();

        String newToken =
                Objects.requireNonNull(newCookie).getValue();

        assertThat(newToken).isNotEqualTo(oldToken);

        assertThat(refreshTokenRepository
                .findByToken(oldToken))
                .isEmpty();

        assertThat(refreshTokenRepository
                .findByToken(newToken))
                .isPresent();
    }

    @Test
    void refresh_withOldTokenAfterRotation_returnsBadRequest()
            throws Exception {

        registerUser();

        Cookie oldCookie = getRefreshCookie();

        mockMvc.perform(post(BASE + "/refresh")
                        .with(csrf())
                        .cookie(oldCookie))
                .andExpect(status().isOk());

        mockMvc.perform(post(BASE + "/refresh")
                        .with(csrf())
                        .cookie(oldCookie))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_withInvalidToken_returnsBadRequest()
            throws Exception {

        Cookie cookie =
                new Cookie(
                        "refresh_token",
                        "invalid-token"
                );

        mockMvc.perform(post(BASE + "/refresh")
                        .with(csrf())
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ===============================================================
    // Refresh Token Cleanup
    // ===============================================================

    @Test
    void cleanupExpiredTokens_removesOnlyExpiredTokens() {

        Account expiredUser = createAccount(
                "Expired User",
                "expired@example.com"
        );

        Account validUser = createAccount(
                "Valid User",
                "valid@example.com"
        );

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setUser(expiredUser);
        expiredToken.setToken("expired-token");
        expiredToken.setExpiryDate(
                Instant.now().minus(1, ChronoUnit.DAYS)
        );

        RefreshToken validToken = new RefreshToken();
        validToken.setUser(validUser);
        validToken.setToken("valid-token");
        validToken.setExpiryDate(
                Instant.now().plus(1, ChronoUnit.DAYS)
        );

        refreshTokenRepository.save(expiredToken);
        refreshTokenRepository.save(validToken);

        refreshTokenService.cleanupExpiredTokens();

        assertThat(refreshTokenRepository
                .findByToken("expired-token"))
                .isEmpty();

        assertThat(refreshTokenRepository
                .findByToken("valid-token"))
                .isPresent();

        assertThat(refreshTokenRepository.count())
                .isEqualTo(1);
    }

    // ===============================================================
    // OTP Verification
    // ===============================================================

    @Test
    void verifyOtp_withValidOtp_returnsPasswordResetToken()
            throws Exception {

        Account account = registerUser();

        String otp = generateAndCaptureOtp(account);

        String request = """
                {
                    "email": "%s",
                    "otp": "%s"
                }
                """.formatted(EMAIL, otp);

        MvcResult result = mockMvc.perform(
                        post(BASE + "/otp/verify")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resetToken").isNotEmpty())
                .andReturn();

        OtpToken token = otpTokenRepository
                .findTopByAccountAndUsedFalseOrderByCreatedAtDesc(account)
                .orElse(null);

        assertThat(token).isNull();

        String resetToken = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("resetToken")
                .asString();

        assertThat(resetToken).isNotBlank();
    }


    @Test
    void verifyOtp_withExpiredOtp_returnsUnauthorized()
            throws Exception {

        Account account = registerUser();

        String otp = "123456";

        OtpToken token = new OtpToken();
        token.setAccount(account);
        token.setOtpHash(passwordEncoder.encode(otp));
        token.setCreatedAt(
                Instant.now().minus(2, ChronoUnit.DAYS)
        );
        token.setExpiresAt(
                Instant.now().minus(1, ChronoUnit.MINUTES)
        );
        token.setUsed(false);
        token.setAttempts(0);

        otpTokenRepository.save(token);

        String request = """
                {
                    "email": "%s",
                    "otp": "%s"
                }
                """.formatted(EMAIL, otp);

        mockMvc.perform(post(BASE + "/otp/verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        OtpToken savedToken =
                otpTokenRepository.findById(token.getId())
                        .orElseThrow();

        assertThat(savedToken.isUsed()).isFalse();
    }

    @Test
    void verifyOtp_cannotReuseAlreadyUsedOtp()
            throws Exception {

        Account account = registerUser();

        String otp = generateAndCaptureOtp(account);

        String request = """
                {
                    "email": "%s",
                    "otp": "%s"
                }
                """.formatted(EMAIL, otp);

        mockMvc.perform(post(BASE + "/otp/verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        mockMvc.perform(post(BASE + "/otp/verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ===============================================================
    // OTP Resend
    // ===============================================================


    @Test
    void resendOtp_generatesNewOtpAndInvalidatesOldOtp()
            throws Exception {

        Account account = registerUser();

        // Create the first OTP
        generateAndCaptureOtp(account);

        OtpToken oldToken = otpTokenRepository
                .findTopByAccountAndUsedFalseOrderByCreatedAtDesc(account)
                .orElseThrow();

        Long oldTokenId = oldToken.getId();

        // Move creation time back to avoid cooldown
        oldToken.setCreatedAt(
                Instant.now().minus(2, ChronoUnit.MINUTES)
        );

        otpTokenRepository.save(oldToken);

        // Clear previous EmailService interactions
        reset(emailService);

        doNothing().when(emailService).sendOtpEmail(
                anyString(),
                anyString(),
                anyString(),
                anyLong()
        );

        ResendOtpRequest request =
                new ResendOtpRequest(EMAIL);

        mockMvc.perform(post(BASE + "/otp/resend")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify old OTP was invalidated
        OtpToken updatedOldToken =
                otpTokenRepository.findById(oldTokenId)
                        .orElseThrow();

        assertThat(updatedOldToken.isUsed()).isTrue();

        // Verify a new unused OTP exists
        OtpToken newToken = otpTokenRepository
                .findTopByAccountAndUsedFalseOrderByCreatedAtDesc(account)
                .orElseThrow();

        assertThat(newToken.getId())
                .isNotEqualTo(oldTokenId);

        // Exactly one email should be sent by THIS resend request
        verify(emailService).sendOtpEmail(
                anyString(),
                anyString(),
                anyString(),
                anyLong()
        );
    }

    // ===============================================================
    // Password Change
    // ===============================================================

    @Test
    void changePassword_withValidResetToken_changesPassword()
            throws Exception {

        Account account = registerUser();

        Map<String, Object> loginResult = loginAndGetResult();

        Cookie refreshCookie =
                (Cookie) loginResult.get("cookie");

        String oldEncodedPassword =
                accountRepository.findByEmail(EMAIL)
                        .orElseThrow()
                        .getPassword();

        String otp = generateAndCaptureOtp(account);

        String resetToken =
                verifyOtpAndGetResetToken(otp);

        SetNewPasswordRequest request =
                new SetNewPasswordRequest(
                        resetToken,
                        NEW_PASSWORD
                );

        mockMvc.perform(post(BASE + "/password/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNoContent());

        Account updatedAccount =
                accountRepository.findByEmail(EMAIL)
                        .orElseThrow();

        assertThat(updatedAccount.getPassword())
                .isNotEqualTo(oldEncodedPassword);

        assertThat(passwordEncoder.matches(
                NEW_PASSWORD,
                updatedAccount.getPassword()
        )).isTrue();

        assertThat(passwordEncoder.matches(
                PASSWORD,
                updatedAccount.getPassword()
        )).isFalse();

        assertThat(updatedAccount.isPasswordChangeRequired())
                .isFalse();

        assertThat(refreshTokenRepository.findByToken(
                Objects.requireNonNull(refreshCookie).getValue()
        )).isEmpty();

        // New password should work
        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(
                                        EMAIL,
                                        NEW_PASSWORD
                                )
                        )))
                .andExpect(status().isOk());

        // Old password should not work
        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(
                                        EMAIL,
                                        PASSWORD
                                )
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_withSamePassword_returnsBadRequest()
            throws Exception {

        Account account = registerUser();

        String otp = generateAndCaptureOtp(account);

        String resetToken =
                verifyOtpAndGetResetToken(otp);

        SetNewPasswordRequest request =
                new SetNewPasswordRequest(
                        resetToken,
                        PASSWORD
                );

        mockMvc.perform(post(BASE + "/password/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(
                                        EMAIL,
                                        PASSWORD
                                )
                        )))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_withInvalidResetToken_doesNotChangePassword()
            throws Exception {

        registerUser();

        Account beforeChange = accountRepository
                .findByEmail(EMAIL)
                .orElseThrow();

        String oldPasswordHash =
                beforeChange.getPassword();

        SetNewPasswordRequest request =
                new SetNewPasswordRequest(
                        "invalid-reset-token",
                        NEW_PASSWORD
                );

        MvcResult result = mockMvc.perform(post(BASE + "/password/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andReturn();

        // Adjust based on your GlobalExceptionHandler if necessary
        assertThat(result.getResponse().getStatus())
                .isIn(400, 401, 403);

        Account afterChange = accountRepository
                .findByEmail(EMAIL)
                .orElseThrow();

        assertThat(afterChange.getPassword())
                .isEqualTo(oldPasswordHash);

        assertThat(passwordEncoder.matches(
                PASSWORD,
                afterChange.getPassword()
        )).isTrue();

        assertThat(passwordEncoder.matches(
                NEW_PASSWORD,
                afterChange.getPassword()
        )).isFalse();
    }

    // ===============================================================
    // Logout
    // ===============================================================

    @Test
    void logout_deletesRefreshTokenAndClearsCookie()
            throws Exception {

        registerUser();

        Map<String, Object> loginResult =
                loginAndGetResult();

        String accessToken =
                (String) loginResult.get("access_token");

        Cookie refreshCookie =
                (Cookie) loginResult.get("cookie");

        String refreshToken =
                Objects.requireNonNull(refreshCookie).getValue();

        Account account =
                accountRepository.findByEmail(EMAIL)
                        .orElseThrow();

        AccountPrincipal principal =
                new AccountPrincipal(account);

        mockMvc.perform(post(BASE + "/logout")
                        .with(csrf())
                        .cookie(refreshCookie)
                        .with(user(principal))
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken
                        ))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(
                        "refresh_token",
                        0
                ));

        assertThat(refreshTokenRepository
                .findByToken(refreshToken))
                .isEmpty();
    }

    @Test
    void logout_withoutRefreshToken_returnsNoContent()
            throws Exception {

        Account account = registerUser();

        AccountPrincipal principal =
                new AccountPrincipal(account);

        mockMvc.perform(post(BASE + "/logout")
                        .with(csrf())
                        .with(user(principal)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(
                        "refresh_token",
                        0
                ));
    }

    // ===============================================================
    // Protected Endpoints
    // ===============================================================

    @Test
    void accessProtectedEndpoint_withoutToken_returnsUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/v1/user/me"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ===============================================================
    // Helper Methods
    // ===============================================================

    private Account registerUser(
            ) throws Exception {

        RegisterRequest request =
                new RegisterRequest(
                        "Jane Doe",
                        AuthIntegrationTest.EMAIL,
                        AuthIntegrationTest.PASSWORD,
                        "123 Main St"
                );

        mockMvc.perform(post(BASE + "/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        return accountRepository
                .findByEmail(AuthIntegrationTest.EMAIL)
                .orElseThrow();
    }

    private Map<String, Object> loginAndGetResult()
            throws Exception {

        MvcResult result = mockMvc.perform(
                        post(BASE + "/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                new LoginRequest(
                                                        EMAIL,
                                                        PASSWORD
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper
                .readTree(
                        result.getResponse()
                                .getContentAsString()
                )
                .get("accessToken")
                .asString();

        Cookie cookie =
                result.getResponse()
                        .getCookie("refresh_token");

        assertThat(cookie).isNotNull();

        return Map.of(
                "cookie",
                cookie,
                "access_token",
                accessToken
        );
    }

    private Cookie getRefreshCookie()
            throws Exception {

        return (Cookie) loginAndGetResult()
                .get("cookie");
    }

    private String generateAndCaptureOtp(Account account) {

        ArgumentCaptor<String> otpCaptor =
                ArgumentCaptor.forClass(String.class);

        // We call the actual service indirectly by resending OTP
        // because OTP is sent through EmailService.
        accountRepository.findById(account.getUserId())
                .orElseThrow();

        // The OTP is generated during resend.
        // Make sure any previous OTP does not trigger cooldown.
        otpTokenRepository
                .findTopByAccountOrderByCreatedAtDesc(account)
                .ifPresent(token -> {
                    token.setCreatedAt(
                            Instant.now()
                                    .minus(2, ChronoUnit.MINUTES)
                    );
                    otpTokenRepository.save(token);
                });

        try {
            mockMvc.perform(post(BASE + "/otp/resend")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "%s"
                                    }
                                    """.formatted(account.getEmail())))
                    .andExpect(status().isNoContent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        verify(emailService).sendOtpEmail(
                anyString(),
                anyString(),
                otpCaptor.capture(),
                anyLong()
        );

        return otpCaptor.getValue();
    }

    private String verifyOtpAndGetResetToken(
            String otp
    ) throws Exception {

        String request = """
                {
                    "email": "%s",
                    "otp": "%s"
                }
                """.formatted(AuthIntegrationTest.EMAIL, otp);

        MvcResult result =
                mockMvc.perform(post(BASE + "/otp/verify")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(request))
                        .andExpect(status().isOk())
                        .andExpect(
                                jsonPath("$.resetToken")
                                        .isNotEmpty()
                        )
                        .andReturn();

        return objectMapper
                .readTree(
                        result.getResponse()
                                .getContentAsString()
                )
                .get("resetToken")
                .asString();
    }

    private Account createAccount(
            String name,
            String email
    ) {

        Account account = new Account();

        account.setName(name);
        account.setEmail(email);
        account.setPassword(
                passwordEncoder.encode("Password123!")
        );
        account.setAddress("Test Address");
        account.setRole(Role.USER);
        account.setCreatedAt(Instant.now());
        account.setUpdatedAt(Instant.now());

        return accountRepository.save(account);
    }
}
