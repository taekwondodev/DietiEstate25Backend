package com.dietiestate25backend.security.authorization;

import com.dietiestate25backend.BaseMvcTest;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Admin Boundary Tests - Register Staff Permission")
class AdminBoundaryTests extends BaseMvcTest {

    @MockitoBean
    private AuthService authService;

    private RegistrazioneRequest staffRegistrationRequest;

    @BeforeEach
    void setUp() {
        staffRegistrationRequest = new RegistrazioneRequest("agente@example.com", "SecurePassword123!", "Gestore");
    }

    @Test
    @DisplayName("Register Staff - Cliente should not be able to register staff (403 Forbidden)")
    void testRegisterStaff_WithClienteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/auth/register-staff")
                .with(jwt().jwt(j -> j.claim("role", "Cliente").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(staffRegistrationRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Register Staff - Agente should not be able to register staff (403 Forbidden)")
    void testRegisterStaff_WithAgenteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/auth/register-staff")
                .with(jwt().jwt(j -> j.claim("role", "AgenteImmobiliare").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(staffRegistrationRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Register Staff - Gestore can also register staff (200 OK)")
    void testRegisterStaff_WithGestoreRole_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/auth/register-staff")
                .with(jwt().jwt(j -> j.claim("role", "Gestore").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(staffRegistrationRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Register Staff - Admin should be able to register staff (200 OK)")
    void testRegisterStaff_WithAdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/auth/register-staff")
                .with(jwt().jwt(j -> j.claim("role", "Admin").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(staffRegistrationRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Register Staff - Unauthenticated user should get 401 Unauthorized")
    void testRegisterStaff_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/auth/register-staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(staffRegistrationRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Register Staff - Invalid role should return 403 Forbidden")
    void testRegisterStaff_WithInvalidRole_ShouldThrowException() throws Exception {
        mockMvc.perform(post("/auth/register-staff")
                .with(jwt().jwt(j -> j.claim("role", "INVALID_ROLE").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(staffRegistrationRequest)))
                .andExpect(status().isForbidden());
    }
}