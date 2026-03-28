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
import com.dietiestate25backend.service.OffertaService;
import com.dietiestate25backend.TestConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Offerta Privacy Tests - Data Isolation")
class OffertaPrivacyTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OffertaService offertaService;

    @Test
    @DisplayName("Riepilogo Offerte Cliente - Cliente can access his own offers (200 OK)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testRiepilogoOfferteCliente_WithClienteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoCliente"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Offerte UtenteAgenzia - Cliente CANNOT access agent offers (403 Forbidden)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testRiepilogoOfferteUtenteAgenzia_WithClienteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoUtenteAgenzia"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Riepilogo Offerte UtenteAgenzia - Admin can access (200 OK)")
    @WithMockUser(username = "admin1", roles = "Admin")
    void testRiepilogoOfferteUtenteAgenzia_WithAdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoUtenteAgenzia"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Offerte UtenteAgenzia - Gestore can access (200 OK)")
    @WithMockUser(username = "gestore1", roles = "Gestore")
    void testRiepilogoOfferteUtenteAgenzia_WithGestoreRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoUtenteAgenzia"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Offerte UtenteAgenzia - AgenteImmobiliare can access (200 OK)")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testRiepilogoOfferteUtenteAgenzia_WithAgenteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoUtenteAgenzia"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Offerte UtenteAgenzia - Unauthenticated user should get 401 Unauthorized")
    void testRiepilogoOfferteUtenteAgenzia_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoUtenteAgenzia"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Riepilogo Offerte Cliente - Only Cliente role can see own offers")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testRiepilogoOfferteCliente_WithAgenteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoCliente"))
                .andExpect(status().isForbidden());
    }
}


