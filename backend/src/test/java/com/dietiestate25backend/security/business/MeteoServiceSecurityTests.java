package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.MeteoDao;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.service.MeteoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * MeteoService Security Tests
 *
 * Verifica che MeteoService validi e rifiuti input malevoli
 * prima di propagarli al layer DAO/API esterna, secondo OWASP:
 *
 * WSTG-INPV-01: Testing for Reflected Cross Site Scripting (via input rejection)
 * WSTG-INPV-05: Testing for SQL Injection (iniezione in parametri coordinate/data)
 * WSTG-BUSL-07: Testing for Circumventing Workflows (date range enforcement)
 */
@DisplayName("MeteoService Security Tests - WSTG-INPV-01, WSTG-INPV-05, WSTG-BUSL-07")
class MeteoServiceSecurityTests extends BaseIntegrationTest {

    @Autowired
    private MeteoService meteoService;

    @MockitoBean
    private MeteoDao meteoDao;

    // ============================================================================
    // OWASP WSTG-INPV-05: Injection in date parameter
    // La data viene usata come parametro nella URL verso l'API esterna;
    // deve essere rifiutata se non rispetta il formato ISO-8601.
    // ============================================================================

    @Test
    @DisplayName("Date - SQL injection payload must be rejected as INVALID_DATE_FORMAT (WSTG-INPV-05)")
    void testOttieniPrevisioni_SqlInjectionInDate_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("43.0", "10.0", "' OR '1'='1"));
        assertEquals(ErrorCode.INVALID_DATE_FORMAT, ex.getErrorCode());
    }

    @Test
    @DisplayName("Date - UNION injection payload must be rejected as INVALID_DATE_FORMAT (WSTG-INPV-05)")
    void testOttieniPrevisioni_UnionInjectionInDate_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("43.0", "10.0", "2026-01-01 UNION SELECT 1--"));
        assertEquals(ErrorCode.INVALID_DATE_FORMAT, ex.getErrorCode());
    }

    @Test
    @DisplayName("Date - XSS payload must be rejected as INVALID_DATE_FORMAT (WSTG-INPV-01)")
    void testOttieniPrevisioni_XssInDate_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("43.0", "10.0", "<script>alert(1)</script>"));
        assertEquals(ErrorCode.INVALID_DATE_FORMAT, ex.getErrorCode());
    }

    @Test
    @DisplayName("Date - malformed ISO string must be rejected as INVALID_DATE_FORMAT (WSTG-INPV-01)")
    void testOttieniPrevisioni_MalformedDate_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("43.0", "10.0", "not-a-date"));
        assertEquals(ErrorCode.INVALID_DATE_FORMAT, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-BUSL-07: Date range enforcement — past dates must be rejected
    // Una data nel passato viene rifiutata con la condizione attuale (pur invertita)
    // perché una data passata è sempre "before(today+7)".
    // ============================================================================

    @Test
    @DisplayName("Date - yesterday must be rejected as INVALID_DATE_RANGE (WSTG-BUSL-07)")
    void testOttieniPrevisioni_PastDate_ShouldThrowBadRequest() {
        String yesterday = LocalDate.now().minusDays(1).toString();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("43.0", "10.0", yesterday));
        assertEquals(ErrorCode.INVALID_DATE_RANGE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Date - date 30 days ago must be rejected as INVALID_DATE_RANGE (WSTG-BUSL-07)")
    void testOttieniPrevisioni_FarPastDate_ShouldThrowBadRequest() {
        String farPast = LocalDate.now().minusDays(30).toString();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("43.0", "10.0", farPast));
        assertEquals(ErrorCode.INVALID_DATE_RANGE, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-INPV-05: Injection in coordinate parameters
    // Le coordinate vengono parsate come Double; qualsiasi stringa non numerica
    // deve essere rifiutata con INVALID_COORDINATES.
    // ============================================================================

    @Test
    @DisplayName("Latitude - SQL OR injection must be rejected as INVALID_COORDINATES (WSTG-INPV-05)")
    void testOttieniPrevisioni_SqlInjectionInLatitude_ShouldThrowBadRequest() {
        String datePassingCheck = LocalDate.now().plusDays(3).toString();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("' OR '1'='1", "10.0", datePassingCheck));
        assertEquals(ErrorCode.INVALID_COORDINATES, ex.getErrorCode());
    }

    @Test
    @DisplayName("Longitude - SQL injection must be rejected as INVALID_COORDINATES (WSTG-INPV-05)")
    void testOttieniPrevisioni_SqlInjectionInLongitude_ShouldThrowBadRequest() {
        String datePassingCheck = LocalDate.now().plusDays(3).toString();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("43.0", "'; DROP TABLE immobile;--", datePassingCheck));
        assertEquals(ErrorCode.INVALID_COORDINATES, ex.getErrorCode());
    }

    @Test
    @DisplayName("Latitude - XSS payload must be rejected as INVALID_COORDINATES (WSTG-INPV-01)")
    void testOttieniPrevisioni_XssInLatitude_ShouldThrowBadRequest() {
        String datePassingCheck = LocalDate.now().plusDays(3).toString();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("<img src=x onerror=alert(1)>", "10.0", datePassingCheck));
        assertEquals(ErrorCode.INVALID_COORDINATES, ex.getErrorCode());
    }

    @Test
    @DisplayName("Latitude - very long string (DoS/buffer probe) must be rejected as INVALID_COORDINATES (WSTG-INPV-13)")
    void testOttieniPrevisioni_LongStringInLatitude_ShouldThrowBadRequest() {
        String datePassingCheck = LocalDate.now().plusDays(3).toString();
        String longPayload = "A".repeat(10000);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni(longPayload, "10.0", datePassingCheck));
        assertEquals(ErrorCode.INVALID_COORDINATES, ex.getErrorCode());
    }

    @Test
    @DisplayName("Latitude - null-byte injection must be rejected as INVALID_COORDINATES (WSTG-INPV-01)")
    void testOttieniPrevisioni_NullByteInLatitude_ShouldThrowBadRequest() {
        String datePassingCheck = LocalDate.now().plusDays(3).toString();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("43.0\u0000--", "10.0", datePassingCheck));
        assertEquals(ErrorCode.INVALID_COORDINATES, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-INPV-01: Coordinate boundary enforcement
    // Latitudine deve essere in [-90, +90]; longitudine in [-180, +180].
    // Valori appena fuori confine devono essere rifiutati.
    // ============================================================================

    @Test
    @DisplayName("Latitude - value below -90 must be rejected as INVALID_LATITUDE (WSTG-INPV-01)")
    void testOttieniPrevisioni_LatitudeBelowMin_ShouldThrowBadRequest() {
        String datePassingCheck = LocalDate.now().plusDays(3).toString();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("-90.001", "10.0", datePassingCheck));
        assertEquals(ErrorCode.INVALID_LATITUDE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Latitude - value above +90 must be rejected as INVALID_LATITUDE (WSTG-INPV-01)")
    void testOttieniPrevisioni_LatitudeAboveMax_ShouldThrowBadRequest() {
        String datePassingCheck = LocalDate.now().plusDays(3).toString();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("90.001", "10.0", datePassingCheck));
        assertEquals(ErrorCode.INVALID_LATITUDE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Longitude - value below -180 must be rejected as INVALID_LONGITUDE (WSTG-INPV-01)")
    void testOttieniPrevisioni_LongitudeBelowMin_ShouldThrowBadRequest() {
        String datePassingCheck = LocalDate.now().plusDays(3).toString();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("43.0", "-180.001", datePassingCheck));
        assertEquals(ErrorCode.INVALID_LONGITUDE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Longitude - value above +180 must be rejected as INVALID_LONGITUDE (WSTG-INPV-01)")
    void testOttieniPrevisioni_LongitudeAboveMax_ShouldThrowBadRequest() {
        String datePassingCheck = LocalDate.now().plusDays(3).toString();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> meteoService.ottieniPrevisioni("43.0", "180.001", datePassingCheck));
        assertEquals(ErrorCode.INVALID_LONGITUDE, ex.getErrorCode());
    }
}
