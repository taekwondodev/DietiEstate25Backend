package com.dietiestate25backend.security.business;

import com.dietiestate25backend.TestConfiguration;
import com.dietiestate25backend.dao.externalimplements.GeoapifyGeoDataDao;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.error.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Geoapify GeoData Exception Handling Tests
 *
 * Business Logic Security - Phase 4
 *
 * Verifica che il DAO lanci eccezioni appropriate quando chiama API esterne
 * senza leakare informazioni sulla struttura della richiesta o errori di rete.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Geoapify GeoData Exception Handling Tests - Business Logic Security")
class GeoapifyGeoDataExceptionHandlingTests {

    @Autowired
    private GeoapifyGeoDataDao geoDataDao;

    @MockitoBean(name = "restTemplate")
    private RestTemplate restTemplate;

    /**
     * Geoapify API ritorna risposta senza "features".
     *
     * Precondizione: API risponde ma manca il campo "features"
     * Azione: ottieniCoordinate() è chiamato
     * Aspettativa: BadRequestException lanciato
     */
    @Test
    @DisplayName("Geoapify returns invalid response - SHOULD throw BadRequestException")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testOttieniCoordinate_InvalidResponse_ShouldThrowBadRequest() {
        Map<String, Object> invalidResponse = new HashMap<>();
        invalidResponse.put("status", "OK");
        // Manca "features"

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(invalidResponse);

        assertThrows(
                BadRequestException.class,
                () -> geoDataDao.ottieniCoordinate("Indirizzo non trovato"),
                "Invalid response dovrebbe lanciare BadRequestException"
        );
    }

    /**
     * Geoapify API ritorna "features" vuoto.
     *
     * Precondizione: API ritorna lista "features" vuota
     * Azione: ottieniCoordinate() è chiamato
     * Aspettativa: BadRequestException lanciato
     */
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
                () -> geoDataDao.ottieniCoordinate("Indirizzo inesistente"),
                "Empty features dovrebbe lanciare BadRequestException"
        );
    }

    /**
     * Geoapify API non è raggiungibile.
     *
     * Precondizione: RestTemplate lancia RestClientException
     * Azione: ottieniCoordinate() è chiamato
     * Aspettativa: InternalServerErrorException lanciato senza leakage
     */
    @Test
    @DisplayName("Geoapify API unreachable - SHOULD wrap without leaking")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testOttieniCoordinate_APIUnreachable_ShouldWrapGeneric() {
        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenThrow(new org.springframework.web.client.RestClientException("Connection refused"));

        assertThrows(
                InternalServerErrorException.class,
                () -> geoDataDao.ottieniCoordinate("Via Roma 10, Genova"),
                "Network error dovrebbe essere wrapped generically"
        );
    }

    /**
     * Categoria di interesse non supportata.
     *
     * Precondizione: Viene richiesta una categoria non nella mappa
     * Azione: ottieniConteggioPuntiInteresse() è chiamato
     * Aspettativa: BadRequestException lanciato
     */
    @Test
    @DisplayName("Unsupported category requested - SHOULD throw BadRequestException")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testOttieniConteggioPuntiInteresse_UnsupportedCategory_ShouldThrowBadRequest() {
        assertThrows(
                BadRequestException.class,
                () -> geoDataDao.ottieniConteggioPuntiInteresse(
                        43.7, 10.4, 500,
                        List.of("categoria_inesistente")
                ),
                "Unsupported category dovrebbe lanciare BadRequestException"
        );
    }

    /**
     * Geoapify API non ritorna "features" per categoria.
     *
     * Precondizione: API risponde ma manca "features"
     * Azione: ottieniConteggioPuntiInteresse() è chiamato
     * Aspettativa: NotFoundException lanciato
     */
    @Test
    @DisplayName("Geoapify returns no features for category - SHOULD throw NotFoundException")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testOttieniConteggioPuntiInteresse_NoFeatures_ShouldThrowNotFound() {
        Map<String, Object> invalidResponse = new HashMap<>();
        invalidResponse.put("status", "OK");
        // Manca "features"

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(invalidResponse);

        assertThrows(
                NotFoundException.class,
                () -> geoDataDao.ottieniConteggioPuntiInteresse(
                        43.7, 10.4, 500,
                        List.of("parco")
                ),
                "Missing features dovrebbe lanciare NotFoundException"
        );
    }
}
