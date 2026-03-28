package com.dietiestate25backend.security.validation;

import com.dietiestate25backend.dto.requests.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.dietiestate25backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dietiestate25backend.TestConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("LoginRequest Validation Tests - Input Security")
class LoginRequestValidationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("LoginRequest - Missing email field should return 400 Bad Request")
    void testLoginRequest_MissingEmail_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"password\":\"SecurePassword123!\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Missing password field should return 400 Bad Request")
    void testLoginRequest_MissingPassword_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"email\":\"user@example.com\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Empty email should return 400 Bad Request")
    void testLoginRequest_EmptyEmail_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest("", "SecurePassword123!");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Empty password should return 400 Bad Request")
    void testLoginRequest_EmptyPassword_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Null email should return 400 Bad Request")
    void testLoginRequest_NullEmail_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"email\":null,\"password\":\"SecurePassword123!\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Null password should return 400 Bad Request")
    void testLoginRequest_NullPassword_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"email\":\"user@example.com\",\"password\":null}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Malformed email should return 400 Bad Request")
    void testLoginRequest_MalformedEmail_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest("not-an-email", "SecurePassword123!");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Email with special characters should be rejected")
    void testLoginRequest_EmailWithSpecialChars_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest("user<script>alert('xss')</script>@example.com", "SecurePassword123!");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Whitespace-only email should be rejected")
    void testLoginRequest_WhitespaceOnlyEmail_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest("   ", "SecurePassword123!");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Whitespace-only password should be rejected")
    void testLoginRequest_WhitespaceOnlyPassword_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "   ");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Extra fields should be ignored (no injection)")
    void testLoginRequest_ExtraFields_ShouldBeIgnored() throws Exception {
        String jsonRequest = "{\"email\":\"user@example.com\",\"password\":\"SecurePassword123!\",\"role\":\"Admin\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isUnauthorized()); // Standard unauthorized, not role-based
    }

    @Test
    @DisplayName("LoginRequest - SQL injection attempt in email should be rejected")
    void testLoginRequest_SqlInjectionInEmail_ShouldBeRejected() throws Exception {
        LoginRequest request = new LoginRequest("'; DROP TABLE utenti; --", "password");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }
}


