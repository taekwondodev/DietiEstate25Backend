package com.dietiestate25backend.security.dataisolation;

import com.dietiestate25backend.BaseMvcTest;
import com.dietiestate25backend.service.VisitaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Visita Privacy Tests - Data Isolation")
class VisitaPrivacyTests extends BaseMvcTest {

    @MockitoBean
    private VisitaService visitaService;

    @Test
    @DisplayName("Riepilogo Visite Cliente - Cliente can access his own visits (200 OK)")
    void testRiepilogoVisiteCliente_WithClienteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/visita/riepilogoCliente")
                .with(jwt().jwt(j -> j.subject("client1").claim("role", "Cliente"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Visite Agente - Cliente CANNOT access agent visits (403 Forbidden)")
    void testRiepilogoVisiteUtenteAgenzia_WithClienteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/visita/riepilogoUtenteAgenzia")
                .with(jwt().jwt(j -> j.subject("client1").claim("role", "Cliente"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Riepilogo Visite Agente - Admin can access (200 OK)")
    void testRiepilogoVisiteUtenteAgenzia_WithAdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/visita/riepilogoUtenteAgenzia")
                .with(jwt().jwt(j -> j.subject("admin1").claim("role", "Admin"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Visite Agente - Gestore can access (200 OK)")
    void testRiepilogoVisiteUtenteAgenzia_WithGestoreRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/visita/riepilogoUtenteAgenzia")
                .with(jwt().jwt(j -> j.subject("gestore1").claim("role", "Gestore"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Visite Agente - AgenteImmobiliare can access (200 OK)")
    void testRiepilogoVisiteUtenteAgenzia_WithAgenteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/visita/riepilogoUtenteAgenzia")
                .with(jwt().jwt(j -> j.subject("agente1").claim("role", "AgenteImmobiliare"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Riepilogo Visite Agente - Unauthenticated user should get 401 Unauthorized")
    void testRiepilogoVisiteUtenteAgenzia_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/visita/riepilogoUtenteAgenzia"))
                .andExpect(status().isUnauthorized());
    }
}
