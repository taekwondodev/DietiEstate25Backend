package com.dietiestate25backend.security.authorization;

import com.dietiestate25backend.BaseMvcTest;
import com.dietiestate25backend.dto.requests.AggiornaVisitaRequest;
import com.dietiestate25backend.dto.requests.PrenotaVisitaRequest;
import com.dietiestate25backend.service.VisitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Visita Boundary Tests - Role Access Control")
class VisitaBoundaryTests extends BaseMvcTest {

    @MockitoBean
    private VisitaService visitaService;

    private PrenotaVisitaRequest prenotaVisitaRequest;
    private AggiornaVisitaRequest aggiornaVisitaRequest;

    @BeforeEach
    void setUp() {
        prenotaVisitaRequest = new PrenotaVisitaRequest(1, LocalDate.now().plusDays(30), LocalTime.of(10, 0));
        aggiornaVisitaRequest = new AggiornaVisitaRequest(1, "Confermata");
    }

    // -------- POST /visita/prenota --------

    @Test
    @DisplayName("Prenota Visita - Unauthenticated user should get 401 Unauthorized")
    void testPrenotaVisita_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/visita/prenota")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prenotaVisitaRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Prenota Visita - Cliente should be able to book a visit (201 Created)")
    void testPrenotaVisita_WithClienteRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/visita/prenota")
                .with(jwt().jwt(j -> j.claim("role", "Cliente").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prenotaVisitaRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Prenota Visita - Admin should be able to book a visit (201 Created)")
    void testPrenotaVisita_WithAdminRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/visita/prenota")
                .with(jwt().jwt(j -> j.claim("role", "Admin").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prenotaVisitaRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Prenota Visita - Gestore should be able to book a visit (201 Created)")
    void testPrenotaVisita_WithGestoreRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/visita/prenota")
                .with(jwt().jwt(j -> j.claim("role", "Gestore").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prenotaVisitaRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Prenota Visita - AgenteImmobiliare should be able to book a visit (201 Created)")
    void testPrenotaVisita_WithAgenteRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/visita/prenota")
                .with(jwt().jwt(j -> j.claim("role", "AgenteImmobiliare").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prenotaVisitaRequest)))
                .andExpect(status().isCreated());
    }

    // -------- PATCH /visita/aggiorna --------

    @Test
    @DisplayName("Aggiorna Visita - Unauthenticated user should get 401 Unauthorized")
    void testAggiornaVisita_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(patch("/visita/aggiorna")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggiornaVisitaRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Aggiorna Visita - Cliente should be able to update a visit (200 OK)")
    void testAggiornaVisita_WithClienteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(patch("/visita/aggiorna")
                .with(jwt().jwt(j -> j.claim("role", "Cliente").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggiornaVisitaRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Aggiorna Visita - Admin should be able to update a visit (200 OK)")
    void testAggiornaVisita_WithAdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(patch("/visita/aggiorna")
                .with(jwt().jwt(j -> j.claim("role", "Admin").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggiornaVisitaRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Aggiorna Visita - Gestore should be able to update a visit (200 OK)")
    void testAggiornaVisita_WithGestoreRole_ShouldReturn200() throws Exception {
        mockMvc.perform(patch("/visita/aggiorna")
                .with(jwt().jwt(j -> j.claim("role", "Gestore").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggiornaVisitaRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Aggiorna Visita - AgenteImmobiliare should be able to update a visit (200 OK)")
    void testAggiornaVisita_WithAgenteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(patch("/visita/aggiorna")
                .with(jwt().jwt(j -> j.claim("role", "AgenteImmobiliare").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggiornaVisitaRequest)))
                .andExpect(status().isOk());
    }
}
