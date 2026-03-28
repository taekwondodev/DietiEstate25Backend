package com.dietiestate25backend.security.authorization;

import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dietiestate25backend.TestConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Admin Boundary Tests - Register Staff Permission")
class AdminBoundaryTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;


    private RegistrazioneRequest staffRegistrationRequest;

    @BeforeEach
    void setUp() {
        staffRegistrationRequest = new RegistrazioneRequest("agente@example.com", "SecurePassword123!", "Gestore");
    }

    @Test
    @DisplayName("Register Staff - Cliente should not be able to register staff (403 Forbidden)")
    @WithMockUser(roles = "Cliente")
    void testRegisterStaff_WithClienteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/auth/register-staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(staffRegistrationRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Register Staff - Agente should not be able to register staff (403 Forbidden)")
    @WithMockUser(roles = "AgenteImmobiliare")
    void testRegisterStaff_WithAgenteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/auth/register-staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(staffRegistrationRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Register Staff - Gestore should not be able to register staff (403 Forbidden)")
    @WithMockUser(roles = "Gestore")
    void testRegisterStaff_WithGestoreRole_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/auth/register-staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(staffRegistrationRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Register Staff - Only Admin should be able to register staff (200 OK)")
    @WithMockUser(roles = "Admin")
    void testRegisterStaff_WithAdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/auth/register-staff")
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
    @DisplayName("Register Staff - Invalid role should throw exception")
    @WithMockUser(username = "user", roles = "INVALID_ROLE")
    void testRegisterStaff_WithInvalidRole_ShouldThrowException() throws Exception {
        mockMvc.perform(post("/auth/register-staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(staffRegistrationRequest)))
                .andExpect(status().isForbidden());
    }
}


