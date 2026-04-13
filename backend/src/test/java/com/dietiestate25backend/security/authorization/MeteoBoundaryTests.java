package com.dietiestate25backend.security.authorization;

import com.dietiestate25backend.BaseMvcTest;
import com.dietiestate25backend.service.MeteoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Meteo Boundary Tests - POST /meteo Access Control")
class MeteoBoundaryTests extends BaseMvcTest {

    @MockitoBean
    private MeteoService meteoService;

    private static final String VALID_BODY =
            "{\"latitudine\":\"40.85\",\"longitudine\":\"14.27\",\"date\":\"2030-01-01\"}";

    @Test
    @DisplayName("Meteo - Unauthenticated user should get 401 Unauthorized")
    void testMeteo_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/meteo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Meteo - Cliente should be able to access (200 OK)")
    void testMeteo_WithClienteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/meteo")
                .with(jwt().jwt(j -> j.claim("role", "Cliente").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Meteo - Admin should be able to access (200 OK)")
    void testMeteo_WithAdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/meteo")
                .with(jwt().jwt(j -> j.claim("role", "Admin").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Meteo - Gestore should be able to access (200 OK)")
    void testMeteo_WithGestoreRole_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/meteo")
                .with(jwt().jwt(j -> j.claim("role", "Gestore").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Meteo - AgenteImmobiliare should be able to access (200 OK)")
    void testMeteo_WithAgenteRole_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/meteo")
                .with(jwt().jwt(j -> j.claim("role", "AgenteImmobiliare").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isOk());
    }
}
