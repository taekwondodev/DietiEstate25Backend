package com.dietiestate25backend.security.auth;

import com.dietiestate25backend.controller.AuthController;
import com.dietiestate25backend.dto.requests.LoginRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.dto.response.LoginResponse;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dietiestate25backend.TestConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("AuthController Security Tests - Request Validation")
class AuthControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private LoginRequest validLoginRequest;
    private RegistrazioneRequest validRegistrazioneRequest;
    private LoginResponse validLoginResponse;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest("user@example.com", "SecurePassword123!");
        validRegistrazioneRequest = new RegistrazioneRequest("newuser@example.com", "SecurePassword123!", "Cliente");
        validLoginResponse = new LoginResponse("valid.jwt.token", "user-id-123", "Cliente");
    }

    @Test
    @DisplayName("Login Request - Missing email should return 400 Bad Request")
    void testLoginRequest_MissingEmail_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"password\":\"SecurePassword123!\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login Request - Missing password should return 400 Bad Request")
    void testLoginRequest_MissingPassword_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"email\":\"user@example.com\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login Request - Empty email should be rejected")
    void testLoginRequest_EmptyEmail_ShouldBeRejected() throws Exception {
        LoginRequest request = new LoginRequest("", "SecurePassword123!");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login Request - Empty password should be rejected")
    void testLoginRequest_EmptyPassword_ShouldBeRejected() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login Request - Malformed email should be rejected")
    void testLoginRequest_MalformedEmail_ShouldBeRejected() throws Exception {
        LoginRequest request = new LoginRequest("not-an-email", "SecurePassword123!");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login Request - Invalid credentials should throw UnauthorizedException")
    void testLoginRequest_InvalidCredentials_ShouldThrowUnauthorizedException() throws Exception {
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new UnauthorizedException("Email o password non corrette"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Registration Request - Missing email should return 400 Bad Request")
    void testRegistrationRequest_MissingEmail_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"password\":\"SecurePassword123!\",\"role\":\"Cliente\"}";

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Registration Request - Missing password should return 400 Bad Request")
    void testRegistrationRequest_MissingPassword_ShouldReturn400() throws Exception {
        String jsonRequest = "{\"email\":\"newuser@example.com\",\"role\":\"Cliente\"}";

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Registration Request - Empty email should be rejected")
    void testRegistrationRequest_EmptyEmail_ShouldBeRejected() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("", "SecurePassword123!", "Cliente");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Registration Request - Empty password should be rejected")
    void testRegistrationRequest_EmptyPassword_ShouldBeRejected() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("newuser@example.com", "", "Cliente");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Registration Request - Malformed email should be rejected")
    void testRegistrationRequest_MalformedEmail_ShouldBeRejected() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("not-an-email", "SecurePassword123!", "Cliente");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Registration Request - Valid registration should return 200 OK")
    void testRegistrationRequest_ValidRegistration_ShouldReturn200() throws Exception {
        doNothing().when(authService).registraCliente(any(RegistrazioneRequest.class));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrazioneRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Login Request - Valid credentials should return 200 OK with token")
    void testLoginRequest_ValidCredentials_ShouldReturn200WithToken() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(validLoginResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.uid").exists())
                .andExpect(jsonPath("$.role").exists());
    }

    @Test
    @DisplayName("Register Staff - Without authentication should return 401 Unauthorized")
    void testRegisterStaff_WithoutAuthentication_ShouldReturn401() throws Exception {
        RegistrazioneRequest request = new RegistrazioneRequest("agente@example.com", "SecurePassword123!", "Gestore");

        mockMvc.perform(post("/auth/register-staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}


