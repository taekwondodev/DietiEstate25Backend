package com.dietiestate25backend.security.dataisolation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.dietiestate25backend.service.VisitaService;
import com.dietiestate25backend.TestConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Visita Privacy Tests - Data Isolation")
class VisitaPrivacyTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VisitaService visitaService;

    @Test
    @DisplayName("Riepilogo Visite Cliente - Cliente can access his own visits (200 OK)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testRiepilogoVisiteCliente_WithClienteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/visita/riepilogoCliente"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Visite Agente - Cliente CANNOT access agent visits (403 Forbidden)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testRiepilogoVisiteUtenteAgenzia_WithClienteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/visita/riepilogoUtenteAgenzia"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Riepilogo Visite Agente - Admin can access (200 OK)")
    @WithMockUser(username = "admin1", roles = "Admin")
    void testRiepilogoVisiteUtenteAgenzia_WithAdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/visita/riepilogoUtenteAgenzia"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Visite Agente - Gestore can access (200 OK)")
    @WithMockUser(username = "gestore1", roles = "Gestore")
    void testRiepilogoVisiteUtenteAgenzia_WithGestoreRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/visita/riepilogoUtenteAgenzia"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Visite Agente - AgenteImmobiliare can access (200 OK)")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testRiepilogoVisiteUtenteAgenzia_WithAgenteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/visita/riepilogoUtenteAgenzia"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Visite Agente - Unauthenticated user should get 401 Unauthorized")
    void testRiepilogoVisiteUtenteAgenzia_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/visita/riepilogoUtenteAgenzia"))
                .andExpect(status().isUnauthorized());
    }
}


