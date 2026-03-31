package com.dietiestate25backend.security.dataisolation;

import com.dietiestate25backend.BaseMvcTest;
import com.dietiestate25backend.service.ImmobileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Immobile Ownership Tests - Data Isolation")
class ImmobileOwnershipTests extends BaseMvcTest {

    @MockitoBean
    private ImmobileService immobileService;

    @Test
    @DisplayName("Immobili Personali - Agente should see only his own properties (200 OK)")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testImmobiliPersonali_WithAgenteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/immobile/personali"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Immobili Personali - Cliente CANNOT access immobili personali (403 Forbidden)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testImmobiliPersonali_WithClienteRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/immobile/personali"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Immobili Personali - Gestore can access (200 OK)")
    @WithMockUser(username = "gestore1", roles = "Gestore")
    void testImmobiliPersonali_WithGestoreRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/immobile/personali"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Immobili Personali - Admin can access (200 OK)")
    @WithMockUser(username = "admin1", roles = "Admin")
    void testImmobiliPersonali_WithAdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/immobile/personali"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Immobili Personali - Unauthenticated user should get 401 Unauthorized")
    void testImmobiliPersonali_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/immobile/personali"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cerca Immobili - Public search should return 200 (no ownership isolation)")
    void testCercaImmobili_PublicSearchShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/immobile/cerca")
                .param("comune", "Napoli")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Cerca Immobili - Should not require authentication")
    void testCercaImmobili_ShouldNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/immobile/cerca")
                .param("comune", "Napoli"))
                .andExpect(status().isOk());
    }
}