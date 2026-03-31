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
}