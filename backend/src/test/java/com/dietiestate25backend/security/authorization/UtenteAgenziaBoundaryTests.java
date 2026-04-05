package com.dietiestate25backend.security.authorization;

import com.dietiestate25backend.BaseMvcTest;
import com.dietiestate25backend.dto.requests.CreaImmobileRequest;
import com.dietiestate25backend.service.ImmobileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UtenteAgenzia Boundary Tests - Immobile Creation Permission")
class UtenteAgenziaBoundaryTests extends BaseMvcTest {

    @MockitoBean
    private ImmobileService immobileService;

    private CreaImmobileRequest creaImmobileRequest;

    @BeforeEach
    void setUp() {
        creaImmobileRequest = new CreaImmobileRequest(
                "http://photo.example.com/photo.jpg",
                "Bellissimo appartamento",
                250000.0,
                85,
                2,
                3,
                "Appartamento",
                "Via Roma 10",
                "Napoli",
                2,
                true,
                true
        );
    }

    @Test
    @DisplayName("Create Immobile - Cliente should not be able to create immobile (403 Forbidden)")
    void testCreateImmobile_WithClienteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/immobile/crea")
                .with(jwt().jwt(j -> j.claim("role", "Cliente").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creaImmobileRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create Immobile - Admin should be able to create immobile (201 Created)")
    void testCreateImmobile_WithAdminRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/immobile/crea")
                .with(jwt().jwt(j -> j.claim("role", "Admin").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creaImmobileRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Create Immobile - Gestore should be able to create immobile (201 Created)")
    void testCreateImmobile_WithGestoreRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/immobile/crea")
                .with(jwt().jwt(j -> j.claim("role", "Gestore").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creaImmobileRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Create Immobile - AgenteImmobiliare should be able to create immobile (201 Created)")
    void testCreateImmobile_WithAgenteRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/immobile/crea")
                .with(jwt().jwt(j -> j.claim("role", "AgenteImmobiliare").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creaImmobileRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Create Immobile - Unauthenticated user should get 401 Unauthorized")
    void testCreateImmobile_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/immobile/crea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creaImmobileRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Immobili Personali - Cliente should not access immobili personali (403 Forbidden)")
    void testImmobiliPersonali_WithClienteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/immobile/personali")
                .with(jwt().jwt(j -> j.claim("role", "Cliente").claim("sub", "test-uid"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Immobili Personali - Admin should be able to access (200 OK)")
    void testImmobiliPersonali_WithAdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/immobile/personali")
                .with(jwt().jwt(j -> j.claim("role", "Admin").claim("sub", "test-uid"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Immobili Personali - Gestore should be able to access (200 OK)")
    void testImmobiliPersonali_WithGestoreRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/immobile/personali")
                .with(jwt().jwt(j -> j.claim("role", "Gestore").claim("sub", "test-uid"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Immobili Personali - AgenteImmobiliare should be able to access (200 OK)")
    void testImmobiliPersonali_WithAgenteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/immobile/personali")
                .with(jwt().jwt(j -> j.claim("role", "AgenteImmobiliare").claim("sub", "test-uid"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Immobili Personali - Unauthenticated user should get 401 Unauthorized")
    void testImmobiliPersonali_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/immobile/personali"))
                .andExpect(status().isUnauthorized());
    }
}
