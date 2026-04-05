package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.externalimplements.OpenMeteoDao;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Open Meteo Weather Exception Handling Tests - Business Logic Security
 *
 * Verifica che il DAO lanci eccezioni appropriate quando chiama API meteo
 * senza leakare informazioni sulla struttura della richiesta o errori di rete.
 */
@DisplayName("Open Meteo Exception Handling Tests - Business Logic Security")
class OpenMeteoExceptionHandlingTests extends BaseIntegrationTest {

    @Autowired
    private OpenMeteoDao openMeteoDao;

    @MockitoBean(name = "restTemplate")
    private RestTemplate restTemplate;

    @Test
    @DisplayName("Open Meteo returns invalid response - SHOULD throw InternalServerErrorException")
    @WithMockUser(username = "user1", roles = "Cliente")
    void testOttieniPrevisioni_InvalidResponse_ShouldThrowInternalError() {
        Map<String, Object> invalidResponse = new HashMap<>();
        invalidResponse.put("latitude", 43.7);

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(invalidResponse);

        assertThrows(
                InternalServerErrorException.class,
                () -> openMeteoDao.ottieniPrevisioni(43.7, 10.4, "2026-04-10")
        );
    }

    @Test
    @DisplayName("Open Meteo date not found - SHOULD throw BadRequestException")
    @WithMockUser(username = "user1", roles = "Cliente")
    void testOttieniPrevisioni_DateNotFound_ShouldThrowBadRequest() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> daily = new HashMap<>();
        daily.put("time", List.of("2026-04-09", "2026-04-10"));
        daily.put("temperature_2m_max", List.of(15.0, 16.0));
        daily.put("temperature_2m_min", List.of(10.0, 11.0));
        daily.put("weathercode", List.of(0, 1));
        response.put("daily", daily);

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(response);

        assertThrows(
                BadRequestException.class,
                () -> openMeteoDao.ottieniPrevisioni(43.7, 10.4, "2026-04-15")
        );
    }

    @Test
    @DisplayName("Open Meteo API unreachable - SHOULD wrap without leaking")
    @WithMockUser(username = "user1", roles = "Cliente")
    void testOttieniPrevisioni_APIUnreachable_ShouldWrapGeneric() {
        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenThrow(new org.springframework.web.client.RestClientException("Connection timeout"));

        assertThrows(
                InternalServerErrorException.class,
                () -> openMeteoDao.ottieniPrevisioni(43.7, 10.4, "2026-04-10")
        );
    }

    @Test
    @DisplayName("Open Meteo daily is null - SHOULD handle gracefully")
    @WithMockUser(username = "user1", roles = "Cliente")
    void testOttieniPrevisioni_DailyIsNull_ShouldWrapGeneric() {
        Map<String, Object> response = new HashMap<>();
        response.put("daily", null);

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(response);

        assertThrows(
                InternalServerErrorException.class,
                () -> openMeteoDao.ottieniPrevisioni(43.7, 10.4, "2026-04-10")
        );
    }

    // ============================================================================
    // OWASP WSTG-ERRH-01: Null API response must surface as EXTERNAL_SERVICE_ERROR
    // Il check esplicito `response == null` deve produrre EXTERNAL_SERVICE_ERROR,
    // distinto da INTERNAL_ERROR, per non rivelare dettagli di parsing al client.
    // ============================================================================

    @Test
    @DisplayName("Open Meteo returns null response - SHOULD throw InternalServerErrorException (WSTG-ERRH-01)")
    @WithMockUser(username = "user1", roles = "Cliente")
    void testOttieniPrevisioni_NullResponse_ShouldThrowInternalError() {
        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(null);

        assertThrows(
                InternalServerErrorException.class,
                () -> openMeteoDao.ottieniPrevisioni(43.7, 10.4, "2026-04-10")
        );
    }

    // ============================================================================
    // OWASP WSTG-ERRH-01: Null entries inside `daily` must not leak NPE details
    // Un API esterna compromessa o difettosa potrebbe omettere campi interni.
    // Il DAO deve gestirli senza propagare lo stack al client.
    // ============================================================================

    @Test
    @DisplayName("Open Meteo daily.time is null - SHOULD wrap NPE without leaking (WSTG-ERRH-01)")
    @WithMockUser(username = "user1", roles = "Cliente")
    void testOttieniPrevisioni_TimeListIsNull_ShouldWrapGeneric() {
        Map<String, Object> daily = new HashMap<>();
        daily.put("time", null);
        daily.put("temperature_2m_max", List.of(20.0));
        daily.put("temperature_2m_min", List.of(10.0));
        daily.put("weathercode", List.of(0));
        Map<String, Object> response = new HashMap<>();
        response.put("daily", daily);

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(response);

        assertThrows(
                InternalServerErrorException.class,
                () -> openMeteoDao.ottieniPrevisioni(43.7, 10.4, "2026-04-10")
        );
    }

    @Test
    @DisplayName("Open Meteo temperature_2m_max is null - SHOULD wrap NPE without leaking (WSTG-ERRH-01)")
    @WithMockUser(username = "user1", roles = "Cliente")
    void testOttieniPrevisioni_TemperatureMaxIsNull_ShouldWrapGeneric() {
        Map<String, Object> daily = new HashMap<>();
        daily.put("time", List.of("2026-04-10"));
        daily.put("temperature_2m_max", null);
        daily.put("temperature_2m_min", List.of(10.0));
        daily.put("weathercode", List.of(0));
        Map<String, Object> response = new HashMap<>();
        response.put("daily", daily);

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(response);

        assertThrows(
                InternalServerErrorException.class,
                () -> openMeteoDao.ottieniPrevisioni(43.7, 10.4, "2026-04-10")
        );
    }

    // ============================================================================
    // OWASP WSTG-ERRH-01: Mismatched array lengths must not produce out-of-bounds leak
    // Se date e temperature hanno lunghezze diverse, IndexOutOfBoundsException
    // deve essere wrappata senza rivelare l'indice o la struttura interna.
    // ============================================================================

    @Test
    @DisplayName("Open Meteo mismatched array lengths - SHOULD wrap IndexOutOfBoundsException (WSTG-ERRH-01)")
    @WithMockUser(username = "user1", roles = "Cliente")
    void testOttieniPrevisioni_MismatchedArrayLengths_ShouldWrapGeneric() {
        Map<String, Object> daily = new HashMap<>();
        daily.put("time", List.of("2026-04-10"));
        daily.put("temperature_2m_max", List.of());   // lunghezza 0, indice 0 → IndexOutOfBoundsException
        daily.put("temperature_2m_min", List.of());
        daily.put("weathercode", List.of());
        Map<String, Object> response = new HashMap<>();
        response.put("daily", daily);

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(response);

        assertThrows(
                InternalServerErrorException.class,
                () -> openMeteoDao.ottieniPrevisioni(43.7, 10.4, "2026-04-10")
        );
    }

    // ============================================================================
    // OWASP WSTG-INPV-11: Type confusion — risposta API con tipo errato
    // Un'API esterna compromessa potrebbe restituire un tipo diverso da Map per `daily`.
    // Il ClassCastException deve essere wrappato senza rivelare dettagli di tipo.
    // ============================================================================

    @Test
    @DisplayName("Open Meteo daily is wrong type (String) - SHOULD wrap ClassCastException (WSTG-INPV-11)")
    @WithMockUser(username = "user1", roles = "Cliente")
    void testOttieniPrevisioni_DailyIsWrongType_ShouldWrapGeneric() {
        Map<String, Object> response = new HashMap<>();
        response.put("daily", "unexpected string payload");

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(response);

        assertThrows(
                InternalServerErrorException.class,
                () -> openMeteoDao.ottieniPrevisioni(43.7, 10.4, "2026-04-10")
        );
    }

    // ============================================================================
    // OWASP WSTG-ERRH-01: Defensive default per weather code sconosciuto
    // Il metodo descriviTempo() deve restituire "Non disponibile" per qualsiasi
    // codice non mappato, senza lanciare eccezioni che leakano logica interna.
    // ============================================================================

    @Test
    @DisplayName("Open Meteo unknown weather code - SHOULD return 'Non disponibile' without exception (WSTG-ERRH-01)")
    @WithMockUser(username = "user1", roles = "Cliente")
    void testOttieniPrevisioni_UnknownWeatherCode_ShouldReturnDefaultDescription() {
        Map<String, Object> daily = new HashMap<>();
        daily.put("time", List.of("2026-04-10"));
        daily.put("temperature_2m_max", List.of(20.0));
        daily.put("temperature_2m_min", List.of(10.0));
        daily.put("weathercode", List.of(999));  // codice sconosciuto
        Map<String, Object> response = new HashMap<>();
        response.put("daily", daily);

        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenReturn(response);

        Map<String, Object> result = openMeteoDao.ottieniPrevisioni(43.7, 10.4, "2026-04-10");

        assertEquals("Non disponibile", result.get("weather_description"),
                "Unknown weather code must fall through to the defensive default, not throw an exception");
    }
}