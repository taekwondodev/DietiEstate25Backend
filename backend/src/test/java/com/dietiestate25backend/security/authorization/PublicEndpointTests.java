package com.dietiestate25backend.security.authorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.dietiestate25backend.service.AuthService;
import com.dietiestate25backend.TestConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Public Endpoint Tests - Access Control")
class PublicEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;


    @Test
    @DisplayName("Login endpoint - Should be publicly accessible (no authentication required)")
    void testLoginEndpoint_ShouldBePublic() throws Exception {
        String jsonRequest = "{\"email\":\"user@example.com\",\"password\":\"password\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest()); // Bad request, but not 401/403
    }

    @Test
    @DisplayName("Register endpoint - Should be publicly accessible (no authentication required)")
    void testRegisterEndpoint_ShouldBePublic() throws Exception {
        String jsonRequest = "{\"email\":\"user@example.com\",\"password\":\"password\",\"role\":\"Cliente\"}";

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest()); // Bad request, but not 401/403
    }

    @Test
    @DisplayName("Cerca Immobili endpoint - Should be publicly accessible (no authentication required)")
    void testCercaImmobiliEndpoint_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/immobile/cerca")
                .param("comune", "Napoli")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk()); // Should be accessible without token
    }

    @Test
    @DisplayName("Register Staff endpoint - Should NOT be publicly accessible (requires authentication)")
    void testRegisterStaffEndpoint_ShouldNotBePublic() throws Exception {
        String jsonRequest = "{\"email\":\"agente@example.com\",\"password\":\"password\",\"role\":\"Gestore\"}";

        mockMvc.perform(post("/auth/register-staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Immobili Personali endpoint - Should NOT be publicly accessible (requires authentication)")
    void testImmobiliPersonaliEndpoint_ShouldNotBePublic() throws Exception {
        mockMvc.perform(get("/immobile/personali"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Create Immobile endpoint - Should NOT be publicly accessible (requires authentication)")
    void testCreaImmobileEndpoint_ShouldNotBePublic() throws Exception {
        String jsonRequest = "{\"urlFoto\":\"http://photo.com/photo.jpg\",\"descrizione\":\"Test\",\"prezzo\":100000}";

        mockMvc.perform(post("/immobile/crea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Prenota Visita endpoint - Should NOT be publicly accessible (requires authentication)")
    void testPrenotaVisitaEndpoint_ShouldNotBePublic() throws Exception {
        String jsonRequest = "{\"idImmobile\":1,\"dataVisita\":\"2026-04-01T10:00:00\"}";

        mockMvc.perform(post("/visita/prenota")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Aggiungi Offerta endpoint - Should NOT be publicly accessible (requires authentication)")
    void testAggiungiOffertaEndpoint_ShouldNotBePublic() throws Exception {
        String jsonRequest = "{\"idImmobile\":1,\"importo\":200000}";

        mockMvc.perform(post("/offerta/aggiungi")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isUnauthorized());
    }
}

