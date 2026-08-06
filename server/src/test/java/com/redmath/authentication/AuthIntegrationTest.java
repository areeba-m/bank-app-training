package com.redmath.authentication;


import com.redmath.account.Account;
import com.redmath.account.AccountRepository;
import com.redmath.account.Role;
import com.redmath.authentication.dto.LoginRequest;
import com.redmath.authentication.dto.RegisterRequest;
import com.redmath.authentication.entity.RefreshToken;
import com.redmath.authentication.repository.RefreshTokenRepository;
import com.redmath.authentication.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    private static final String BASE = "/api/v1/auth";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private static final String EMAIL = "jane.doe@example.com";
    private static final String PASSWORD = "S3curePassw0rd!";

    @BeforeEach
    void cleanDb() {
        refreshTokenRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        accountRepository.deleteAll();
    }

    // ---------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------

    @Test
    void register_withNewEmail_createsAccountAndReturns201() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", EMAIL, PASSWORD, "123 Main St");

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.userId").exists());

        assertThat(accountRepository.existsByEmail(EMAIL)).isTrue();
    }

    @Test
    void register_withDuplicateEmail_isRejected() throws Exception {
        registerUser(EMAIL, PASSWORD);

        RegisterRequest duplicate = new RegisterRequest(
                "Someone Else", EMAIL, "AnotherPass1!", "456 Side St");

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().is4xxClientError());

        assertThat(accountRepository.count()).isEqualTo(1);
    }

    @Test
    void register_withInvalidPayload_returns400() throws Exception {
        String malformed = """
                {"name": "", "email": "not-an-email", "password": "", "address": ""}
                """;

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformed))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------

    @Test
    void login_succeeds_and_setsRefreshCookie() throws Exception {
        registerUser(EMAIL, PASSWORD);
        LoginRequest login = new LoginRequest(EMAIL, PASSWORD);

        MvcResult result = mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("refresh_token", true))
                .andReturn();

        Cookie refreshCookie = result.getResponse().getCookie("refresh_token");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshTokenRepository.findByToken(
                Objects.requireNonNull(refreshCookie).getValue()))
                .isPresent();
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        registerUser(EMAIL, PASSWORD);
        LoginRequest badLogin = new LoginRequest(EMAIL, "wrong-password");

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withUnknownEmail_returns401() throws Exception {
        LoginRequest login = new LoginRequest("nobody@example.com", PASSWORD);

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------
    // Refresh
    // ---------------------------------------------------------------

    @Test
    void refresh_withValidCookie_rotatesTokenAndReturnsNewAccessToken() throws Exception {
        registerUser(EMAIL, PASSWORD);
        Cookie initialRefreshCookie = (Cookie) loginAndGetResult().get("cookie");

        MvcResult refreshResult = mockMvc.perform(post(BASE + "/refresh")
                        .with(csrf().asHeader())
                        .cookie(initialRefreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        Cookie rotatedCookie = refreshResult.getResponse().getCookie("refresh_token");
        assertThat(rotatedCookie).isNotNull();
        assertThat(rotatedCookie.getValue()).isNotEqualTo(initialRefreshCookie.getValue());

        // old token should no longer be valid after rotation
        mockMvc.perform(post(BASE + "/refresh").cookie(initialRefreshCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void refresh_withMissingCookie_returns4xx() throws Exception {
        mockMvc.perform(post(BASE + "/refresh"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void refresh_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(post(BASE + "/refresh").with(csrf().asHeader())
                .cookie(new Cookie("refresh_token", "not-a-real-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cleanupExpiredTokens_removesOnlyExpiredTokens() {
        Account expiredUser = new Account();
        expiredUser.setName("Expired User");
        expiredUser.setEmail("expired@test.com");
        expiredUser.setPassword("password");
        expiredUser.setAddress("Address");
        expiredUser.setRole(Role.USER);
        expiredUser.setCreatedAt(Instant.now());
        expiredUser.setUpdatedAt(Instant.now());
        accountRepository.save(expiredUser);

        Account validUser = new Account();
        validUser.setName("Valid User");
        validUser.setEmail("valid@test.com");
        validUser.setPassword("password");
        validUser.setAddress("Address");
        validUser.setRole(Role.USER);
        validUser.setCreatedAt(Instant.now());
        validUser.setUpdatedAt(Instant.now());
        accountRepository.save(validUser);

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setUser(expiredUser);
        expiredToken.setToken("expired-token");
        expiredToken.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));

        RefreshToken validToken = new RefreshToken();
        validToken.setUser(validUser);
        validToken.setToken("valid-token");
        validToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));

        refreshTokenRepository.save(expiredToken);
        refreshTokenRepository.save(validToken);

        refreshTokenService.cleanupExpiredTokens();

        assertThat(refreshTokenRepository.findByToken("expired-token")).isEmpty();
        assertThat(refreshTokenRepository.findByToken("valid-token")).isPresent();
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
    }

    // ---------------------------------------------------------------
    // Logout
    // ---------------------------------------------------------------

    @Test
    void logout_deletesRefreshTokenAndClearsCookie() throws Exception {
        registerUser(EMAIL, PASSWORD);

        Map<String, Object> loginResult = loginAndGetResult();
        String accessToken = (String) loginResult.get("access_token");
        Cookie refreshCookie = (Cookie) loginResult.get("cookie");
        String tokenValue = refreshCookie.getValue();

        assertThat(refreshTokenRepository.findByToken(tokenValue)).isPresent();

        mockMvc.perform(post(BASE + "/logout")
                        .cookie(refreshCookie)
                        .with(csrf().asHeader())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0));

        assertThat(refreshTokenRepository.findByToken(tokenValue)).isEmpty();
    }

    @Test
    void logout_withoutCookie_stillReturnsNoContent() throws Exception {
        registerUser(EMAIL, PASSWORD);

        Map<String, Object> loginResult = loginAndGetResult();
        String accessToken = (String) loginResult.get("access_token");

        mockMvc.perform(post(BASE + "/logout").with(csrf().asHeader())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    // ---------------------------------------------------------------
    // Access control on protected endpoints
    // ---------------------------------------------------------------

    @Test
    void accessAdminEndpoint_withUserToken_isForbidden() throws Exception {
        registerUser(EMAIL, PASSWORD);
        String accessToken = (String) loginAndGetResult().get("access_token");

        mockMvc.perform(get("/api/v1/admin/anything")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessProtectedUserEndpoint_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    void accessProtectedUserEndpoint_withValidAccessToken_succeeds() throws Exception {
//        registerUser(EMAIL, PASSWORD);
//        String accessToken = (String) loginAndGetResult().get("access_token");
//
//        mockMvc.perform(get("/api/v1/user/1").with(csrf().asHeader())
//                        .header("Authorization", "Bearer " + accessToken))
//                .andExpect(status().isOk());
//    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private void registerUser(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", email, password, "123 Main St");
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private Map<String, Object> loginAndGetResult() throws Exception {
        LoginRequest login = new LoginRequest(EMAIL, PASSWORD);
        MvcResult result = mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String loginBody = result.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(loginBody).get("accessToken").asString();

        Cookie cookie = result.getResponse().getCookie("refresh_token");

        assertThat(cookie).isNotNull();
        return Map.of("cookie", cookie, "access_token", accessToken);
    }

}
