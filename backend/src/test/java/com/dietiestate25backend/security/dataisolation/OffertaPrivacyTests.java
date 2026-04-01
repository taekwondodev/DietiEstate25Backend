package com.dietiestate25backend.security.dataisolation;

import com.dietiestate25backend.BaseMvcTest;
import com.dietiestate25backend.service.OffertaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Offerta Privacy Tests - Data Isolation")
class OffertaPrivacyTests extends BaseMvcTest {

    @MockitoBean
    private OffertaService offertaService;

    @Test
    @DisplayName("Riepilogo Offerte Cliente - Cliente can access his own offers (200 OK)")
    void testRiepilogoOfferteCliente_WithClienteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoCliente")
                .with(jwt().jwt(j -> j.subject("client1").claim("role", "Cliente"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Offerte UtenteAgenzia - Cliente CANNOT access agent offers (403 Forbidden)")
    void testRiepilogoOfferteUtenteAgenzia_WithClienteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoUtenteAgenzia")
                .with(jwt().jwt(j -> j.subject("client1").claim("role", "Cliente"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Riepilogo Offerte UtenteAgenzia - Admin can access (200 OK)")
    void testRiepilogoOfferteUtenteAgenzia_WithAdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoUtenteAgenzia")
                .with(jwt().jwt(j -> j.subject("admin1").claim("role", "Admin"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Offerte UtenteAgenzia - Gestore can access (200 OK)")
    void testRiepilogoOfferteUtenteAgenzia_WithGestoreRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoUtenteAgenzia")
                .with(jwt().jwt(j -> j.subject("gestore1").claim("role", "Gestore"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Offerte UtenteAgenzia - AgenteImmobiliare can access (200 OK)")
    void testRiepilogoOfferteUtenteAgenzia_WithAgenteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoUtenteAgenzia")
                .with(jwt().jwt(j -> j.subject("agente1").claim("role", "AgenteImmobiliare"))))
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
    void testRiepilogoOfferteCliente_WithAgenteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/offerta/riepilogoCliente")
                .with(jwt().jwt(j -> j.subject("agente1").claim("role", "AgenteImmobiliare"))))
                .andExpect(status().isForbidden());
    }
}
