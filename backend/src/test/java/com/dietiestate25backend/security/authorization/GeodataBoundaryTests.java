package com.dietiestate25backend.security.authorization;

import com.dietiestate25backend.BaseMvcTest;
import com.dietiestate25backend.dto.requests.ConteggioPuntiInteresseRequest;
import com.dietiestate25backend.service.GeoDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Geodata Boundary Tests - POST /geodata Access Control - WSTG-AUTHZ-03")
class GeodataBoundaryTests extends BaseMvcTest {

    @MockitoBean
    private GeoDataService geoDataService;

    private ConteggioPuntiInteresseRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new ConteggioPuntiInteresseRequest(40.85, 14.27, 1000, List.of("catering.restaurant"));
    }

    @Test
    @DisplayName("Geodata - Unauthenticated user should get 401 Unauthorized")
    void testGeodata_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/geodata")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Geodata - Cliente should be able to access (201 Created)")
    void testGeodata_WithClienteRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/geodata")
                .with(jwt().jwt(j -> j.claim("role", "Cliente").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Geodata - Admin should be able to access (201 Created)")
    void testGeodata_WithAdminRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/geodata")
                .with(jwt().jwt(j -> j.claim("role", "Admin").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Geodata - Gestore should be able to access (201 Created)")
    void testGeodata_WithGestoreRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/geodata")
                .with(jwt().jwt(j -> j.claim("role", "Gestore").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Geodata - AgenteImmobiliare should be able to access (201 Created)")
    void testGeodata_WithAgenteRole_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/geodata")
                .with(jwt().jwt(j -> j.claim("role", "AgenteImmobiliare").claim("sub", "test-uid")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }
}
