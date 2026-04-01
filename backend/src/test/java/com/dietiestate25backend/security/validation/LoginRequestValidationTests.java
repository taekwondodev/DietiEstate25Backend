package com.dietiestate25backend.security.validation;

import com.dietiestate25backend.BaseMvcTest;
import com.dietiestate25backend.dto.requests.LoginRequest;
import com.dietiestate25backend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("LoginRequest Validation Tests - Input Security")
class LoginRequestValidationTests extends BaseMvcTest {

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

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Empty password should return 400 Bad Request")
    void testLoginRequest_EmptyPassword_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
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

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Email with special characters should be rejected")
    void testLoginRequest_EmailWithSpecialChars_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest("user<script>alert('xss')</script>@example.com", "SecurePassword123!");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Whitespace-only email should be rejected")
    void testLoginRequest_WhitespaceOnlyEmail_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest("   ", "SecurePassword123!");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Whitespace-only password should be rejected")
    void testLoginRequest_WhitespaceOnlyPassword_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "   ");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - Extra fields should return 400 Bad Request")
    void testLoginRequest_ExtraFields_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"email\":\"user@example.com\",\"password\":\"SecurePassword123!\",\"role\":\"Admin\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("LoginRequest - SQL injection attempt in email should be rejected")
    void testLoginRequest_SqlInjectionInEmail_ShouldBeRejected() throws Exception {
        LoginRequest request = new LoginRequest("'; DROP TABLE utenti; --", "password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}