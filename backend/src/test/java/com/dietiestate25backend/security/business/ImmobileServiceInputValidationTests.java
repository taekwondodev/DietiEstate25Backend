package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.dto.requests.CreaImmobileRequest;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.DatabaseErrorException;
import com.dietiestate25backend.service.GeoDataService;
import com.dietiestate25backend.service.ImmobileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ImmobileService Input Validation Security Tests
 *
 * Verifica che ImmobileService applichi i controlli di input prima
 * di raggiungere il layer DAO o i servizi esterni, secondo OWASP:
 *
 * WSTG-INPV-01: Testing for Input Validation (null/blank guards, coordinate map integrity)
 * WSTG-BUSL-07: Testing for Circumventing Workflows (boundary bypass su prezzi, dimensioni, paginazione)
 * WSTG-ERRH-01: Testing for Improper Error Handling (DAO failures wrapped correttamente)
 */
@DisplayName("ImmobileService Input Validation Security Tests - WSTG-INPV-01, WSTG-BUSL-07, WSTG-ERRH-01")
class ImmobileServiceInputValidationTests extends BaseIntegrationTest {

    @Autowired
    private ImmobileService immobileService;

    @MockitoBean
    private ImmobileDao immobileDao;

    @MockitoBean
    private GeoDataService geoDataService;

    private CreaImmobileRequest buildValidRequest() {
        return new CreaImmobileRequest(
                "image.jpg", "Appartamento luminoso", 250000.0,
                80, 2, 3, "Appartamento",
                "Via Roma 10", "Genova", 2, true, false
        );
    }

    // ============================================================================
    // OWASP WSTG-BUSL-07: Paginazione — page size fuori limite
    // Attacker aumenta `size` per scaricare l'intero dataset in una chiamata.
    // ============================================================================

    @Test
    @DisplayName("cercaImmobili - size > 100 must be rejected as INVALID_PAGE_SIZE (WSTG-BUSL-07)")
    void testCercaImmobili_PageSizeExceedsLimit_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.cercaImmobili("Genova", null, null, null, null, null, 0, 101));
        assertEquals(ErrorCode.INVALID_PAGE_SIZE, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-BUSL-07: Filtri prezzo — valori negativi e range invertito
    // Valori negativi o range invertiti potrebbero causare query con risultati
    // imprevedibili o leak di tutti gli immobili disponibili.
    // ============================================================================

    @Test
    @DisplayName("cercaImmobili - negative prezzoMin must be rejected as INVALID_PRICE (WSTG-BUSL-07)")
    void testCercaImmobili_NegativePrezzoMin_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.cercaImmobili("Genova", null, -1.0, 500000.0, null, null, 0, 10));
        assertEquals(ErrorCode.INVALID_PRICE, ex.getErrorCode());
    }

    @Test
    @DisplayName("cercaImmobili - negative prezzoMax must be rejected as INVALID_PRICE (WSTG-BUSL-07)")
    void testCercaImmobili_NegativePrezzoMax_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.cercaImmobili("Genova", null, 100000.0, -1.0, null, null, 0, 10));
        assertEquals(ErrorCode.INVALID_PRICE, ex.getErrorCode());
    }

    @Test
    @DisplayName("cercaImmobili - prezzoMin > prezzoMax must be rejected as INVALID_PRICE_RANGE (WSTG-BUSL-07)")
    void testCercaImmobili_InvertedPriceRange_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.cercaImmobili("Genova", null, 500000.0, 100000.0, null, null, 0, 10));
        assertEquals(ErrorCode.INVALID_PRICE_RANGE, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-BUSL-07: Filtri dimensione e bagni — valori negativi
    // ============================================================================

    @Test
    @DisplayName("cercaImmobili - negative dimensione must be rejected as INVALID_DIMENSION (WSTG-BUSL-07)")
    void testCercaImmobili_NegativeDimensione_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.cercaImmobili("Genova", null, null, null, -1.0, null, 0, 10));
        assertEquals(ErrorCode.INVALID_DIMENSION, ex.getErrorCode());
    }

    @Test
    @DisplayName("cercaImmobili - negative nBagni must be rejected as INVALID_BATHROOMS (WSTG-BUSL-07)")
    void testCercaImmobili_NegativeNBagni_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.cercaImmobili("Genova", null, null, null, null, -1, 0, 10));
        assertEquals(ErrorCode.INVALID_BATHROOMS, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-INPV-01: Identity field tampering — null/blank uidResponsabile
    // ============================================================================

    @Test
    @DisplayName("creaImmobile - null uidResponsabile must be rejected before any external call (WSTG-INPV-01)")
    void testCreaImmobile_NullUidResponsabile_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.creaImmobile(buildValidRequest(), null));
        assertEquals(ErrorCode.INVALID_RESPONSABILE, ex.getErrorCode());
    }

    @Test
    @DisplayName("creaImmobile - blank uidResponsabile must be rejected before any external call (WSTG-INPV-01)")
    void testCreaImmobile_BlankUidResponsabile_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.creaImmobile(buildValidRequest(), "   "));
        assertEquals(ErrorCode.INVALID_RESPONSABILE, ex.getErrorCode());
    }

    @Test
    @DisplayName("immobiliPersonali - null uidResponsabile must be rejected before DAO call (WSTG-INPV-01)")
    void testImmobiliPersonali_NullUidResponsabile_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.immobiliPersonali(null));
        assertEquals(ErrorCode.INVALID_RESPONSABILE, ex.getErrorCode());
    }

    @Test
    @DisplayName("immobiliPersonali - blank uidResponsabile must be rejected before DAO call (WSTG-INPV-01)")
    void testImmobiliPersonali_BlankUidResponsabile_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.immobiliPersonali("   "));
        assertEquals(ErrorCode.INVALID_RESPONSABILE, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-INPV-01: Coordinate map integrity
    // Un attaccante che controlla il servizio geo potrebbe rispondere con una mappa
    // parziale. Il service deve rifiutare qualsiasi risposta incompleta.
    // ============================================================================

    @Test
    @DisplayName("creaImmobile - coordinate map missing 'latitudine' key must be rejected as INVALID_ADDRESS (WSTG-INPV-01)")
    void testCreaImmobile_CoordinateMissingLatitudine_ShouldThrowBadRequest() {
        when(geoDataService.ottieniCoordinate(anyString(), anyString()))
                .thenReturn(Map.of("longitudine", 9.2));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.creaImmobile(buildValidRequest(), "agente-uid"));
        assertEquals(ErrorCode.INVALID_ADDRESS, ex.getErrorCode());
    }

    @Test
    @DisplayName("creaImmobile - coordinate map missing 'longitudine' key must be rejected as INVALID_ADDRESS (WSTG-INPV-01)")
    void testCreaImmobile_CoordinateMissingLongitudine_ShouldThrowBadRequest() {
        when(geoDataService.ottieniCoordinate(anyString(), anyString()))
                .thenReturn(Map.of("latitudine", 44.4));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> immobileService.creaImmobile(buildValidRequest(), "agente-uid"));
        assertEquals(ErrorCode.INVALID_ADDRESS, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-ERRH-01: Error handling — DAO write failure must not leak internals
    // ============================================================================

    @Test
    @DisplayName("creaImmobile - DAO returns false must produce DatabaseErrorException (WSTG-ERRH-01)")
    void testCreaImmobile_DaoReturnsFalse_ShouldThrowDatabaseError() {
        when(geoDataService.ottieniCoordinate(anyString(), anyString()))
                .thenReturn(Map.of("latitudine", 44.4, "longitudine", 9.2));
        when(immobileDao.creaImmobile(any())).thenReturn(false);

        assertThrows(DatabaseErrorException.class,
                () -> immobileService.creaImmobile(buildValidRequest(), "agente-uid"));
    }
}
