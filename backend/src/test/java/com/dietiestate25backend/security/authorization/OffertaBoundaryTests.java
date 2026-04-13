package com.dietiestate25backend.security.authorization;

import com.dietiestate25backend.BaseMvcTest;
import com.dietiestate25backend.dto.requests.AggiornaOffertaRequest;
import com.dietiestate25backend.dto.requests.CreaOffertaRequest;
import com.dietiestate25backend.service.OffertaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Offerta Boundary Tests - Role Access Control")
class OffertaBoundaryTests extends BaseMvcTest {

    @MockitoBean
    private OffertaService offertaService;

    private CreaOffertaRequest creaOffertaRequest;
    private AggiornaOffertaRequest aggiornaOffertaRequest;

    @BeforeEach
    void setUp() {
        creaOffertaRequest = new CreaOffertaRequest(200000.0, 1);
        aggiornaOffertaRequest = new AggiornaOffertaRequest(1, "Accettata");
    }

    // -------- POST /offerta/aggiungi --------

    @Test
    @DisplayName("Aggiungi Offerta - Unauthenticated user should get 401 Unauthorized")
    void testAggiungiOfferta_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/offerta/aggiungi")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creaOffertaRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Aggiungi Offerta - Cliente should be able to add offer (201 Created)")
    void testAggiungiOfferta_WithClienteRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/offerta/aggiungi")
                .with(jwt().jwt(j -> j.claim("role", "Cliente").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creaOffertaRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Aggiungi Offerta - Admin should be able to add offer (201 Created)")
    void testAggiungiOfferta_WithAdminRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/offerta/aggiungi")
                .with(jwt().jwt(j -> j.claim("role", "Admin").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creaOffertaRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Aggiungi Offerta - Gestore should be able to add offer (201 Created)")
    void testAggiungiOfferta_WithGestoreRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/offerta/aggiungi")
                .with(jwt().jwt(j -> j.claim("role", "Gestore").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creaOffertaRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Aggiungi Offerta - AgenteImmobiliare should be able to add offer (201 Created)")
    void testAggiungiOfferta_WithAgenteRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/offerta/aggiungi")
                .with(jwt().jwt(j -> j.claim("role", "AgenteImmobiliare").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creaOffertaRequest)))
                .andExpect(status().isCreated());
    }

    // -------- PATCH /offerta/aggiorna --------

    @Test
    @DisplayName("Aggiorna Offerta - Unauthenticated user should get 401 Unauthorized")
    void testAggiornaOfferta_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(patch("/offerta/aggiorna")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggiornaOffertaRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Aggiorna Offerta - Cliente should be able to update offer (200 OK)")
    void testAggiornaOfferta_WithClienteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(patch("/offerta/aggiorna")
                .with(jwt().jwt(j -> j.claim("role", "Cliente").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggiornaOffertaRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Aggiorna Offerta - Admin should be able to update offer (200 OK)")
    void testAggiornaOfferta_WithAdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(patch("/offerta/aggiorna")
                .with(jwt().jwt(j -> j.claim("role", "Admin").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggiornaOffertaRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Aggiorna Offerta - Gestore should be able to update offer (200 OK)")
    void testAggiornaOfferta_WithGestoreRole_ShouldReturn200() throws Exception {
        mockMvc.perform(patch("/offerta/aggiorna")
                .with(jwt().jwt(j -> j.claim("role", "Gestore").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggiornaOffertaRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Aggiorna Offerta - AgenteImmobiliare should be able to update offer (200 OK)")
    void testAggiornaOfferta_WithAgenteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(patch("/offerta/aggiorna")
                .with(jwt().jwt(j -> j.claim("role", "AgenteImmobiliare").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggiornaOffertaRequest)))
                .andExpect(status().isOk());
    }
}
