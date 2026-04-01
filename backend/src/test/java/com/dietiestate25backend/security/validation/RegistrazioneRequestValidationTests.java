package com.dietiestate25backend.security.validation;

import com.dietiestate25backend.BaseMvcTest;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("RegistrazioneRequest Validation Tests - Input Security")
class RegistrazioneRequestValidationTests extends BaseMvcTest {

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("RegistrazioneRequest - Missing email field should return 400 Bad Request")
    void testRegistrazioneRequest_MissingEmail_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"password\":\"SecurePassword123!\",\"role\":\"Cliente\"}";

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Missing password field should return 400 Bad Request")
    void testRegistrazioneRequest_MissingPassword_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"email\":\"newuser@example.com\",\"role\":\"Cliente\"}";

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Missing role field should return 400 Bad Request")
    void testRegistrazioneRequest_MissingRole_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"email\":\"newuser@example.com\",\"password\":\"SecurePassword123!\"}";

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Empty email should return 400 Bad Request")
    void testRegistrazioneRequest_EmptyEmail_ShouldReturn400() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("", "SecurePassword123!", "Cliente");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Empty password should return 400 Bad Request")
    void testRegistrazioneRequest_EmptyPassword_ShouldReturn400() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("newuser@example.com", "", "Cliente");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Empty role should return 400 Bad Request")
    void testRegistrazioneRequest_EmptyRole_ShouldReturn400() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("newuser@example.com", "SecurePassword123!", "");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Null email should return 400 Bad Request")
    void testRegistrazioneRequest_NullEmail_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"email\":null,\"password\":\"SecurePassword123!\",\"role\":\"Cliente\"}";

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Malformed email should return 400 Bad Request")
    void testRegistrazioneRequest_MalformedEmail_ShouldReturn400() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("not-an-email", "SecurePassword123!", "Cliente");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Email with XSS attempt should be rejected")
    void testRegistrazioneRequest_EmailWithXssAttempt_ShouldBeRejected() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("<script>alert('xss')</script>@example.com", "SecurePassword123!", "Cliente");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Whitespace-only email should be rejected")
    void testRegistrazioneRequest_WhitespaceOnlyEmail_ShouldReturn400() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("   ", "SecurePassword123!", "Cliente");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Whitespace-only password should be rejected")
    void testRegistrazioneRequest_WhitespaceOnlyPassword_ShouldReturn400() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("newuser@example.com", "   ", "Cliente");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Extra fields should return 400 Bad Request")
    void testRegistrazioneRequest_ExtraFields_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"email\":\"newuser@example.com\",\"password\":\"SecurePassword123!\",\"role\":\"Cliente\",\"admin\":true}";

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - SQL injection attempt should be rejected")
    void testRegistrazioneRequest_SqlInjectionAttempt_ShouldBeRejected() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("'; DROP TABLE utenti; --", "password", "Cliente");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RegistrazioneRequest - Password with only numbers should be rejected")
    void testRegistrazioneRequest_NumericPassword_ShouldBeRejected() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("user@example.com", "12345", "Cliente");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}