package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.externalimplements.GeoapifyGeoDataDao;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Geoapify GeoData Exception Handling Tests - Business Logic Security
 *
 * Verifica che il DAO lanci eccezioni appropriate quando chiama API esterne
 * senza leakare informazioni sulla struttura della richiesta o errori di rete.
 */
@DisplayName("Geoapify GeoData Exception Handling Tests - Business Logic Security")
class GeoapifyGeoDataExceptionHandlingTests extends BaseIntegrationTest {

    @Autowired
    private GeoapifyGeoDataDao geoDataDao;

    @MockitoBean(name = "restTemplate")
    private RestTemplate restTemplate;

    @Test
    @DisplayName("Geoapify returns invalid response - SHOULD throw InternalServerErrorException")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testOttieniCoordinate_InvalidResponse_ShouldThrowInternalError() {
        Map<String, Object> invalidResponse = new HashMap<>();
        invalidResponse.put("status", "OK");

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(invalidResponse);

        assertThrows(
                InternalServerErrorException.class,
                () -> geoDataDao.ottieniCoordinate("Indirizzo non trovato")
        );
    }

    @Test
    @DisplayName("Geoapify returns empty features - SHOULD throw BadRequestException")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testOttieniCoordinate_EmptyFeatures_ShouldThrowBadRequest() {
        Map<String, Object> response = new HashMap<>();
        response.put("features", List.of());

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(response);

        assertThrows(
                BadRequestException.class,
                () -> geoDataDao.ottieniCoordinate("Indirizzo inesistente")
        );
    }

    @Test
    @DisplayName("Geoapify API unreachable - SHOULD wrap without leaking")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testOttieniCoordinate_APIUnreachable_ShouldWrapGeneric() {
        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenThrow(new org.springframework.web.client.RestClientException("Connection refused"));

        assertThrows(
                InternalServerErrorException.class,
                () -> geoDataDao.ottieniCoordinate("Via Roma 10, Genova")
        );
    }

    @Test
    @DisplayName("Unsupported category requested - SHOULD throw BadRequestException")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testOttieniConteggioPuntiInteresse_UnsupportedCategory_ShouldThrowBadRequest() {
        assertThrows(
                BadRequestException.class,
                () -> geoDataDao.ottieniConteggioPuntiInteresse(43.7, 10.4, 500, List.of("categoria_inesistente"))
        );
    }

    @Test
    @DisplayName("Geoapify returns no features for category - SHOULD throw InternalServerErrorException")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testOttieniConteggioPuntiInteresse_NoFeatures_ShouldThrowInternalError() {
        Map<String, Object> invalidResponse = new HashMap<>();
        invalidResponse.put("status", "OK");

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(invalidResponse);

        assertThrows(
                InternalServerErrorException.class,
                () -> geoDataDao.ottieniConteggioPuntiInteresse(43.7, 10.4, 500, List.of("parco"))
        );
    }
}