package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.GeoDataDao;
import com.dietiestate25backend.dto.requests.ConteggioPuntiInteresseRequest;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.service.GeoDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GeoDataService Security Tests
 *
 * Verifica le proprietà di sicurezza di GeoDataService come service layer:
 *
 * WSTG-ERRH-01: Exception propagation — BadRequestException e InternalServerErrorException
 *               dal DAO non vengono silenziate dal service layer.
 * WSTG-INPV-01: Address concatenation integrity — ottieniCoordinate concatena
 *               indirizzo e comune e li passa al DAO senza manipolazione aggiuntiva.
 */
@DisplayName("GeoDataService Security Tests - WSTG-ERRH-01, WSTG-INPV-01")
class GeoDataExceptionHandlingTests extends BaseIntegrationTest {

    @Autowired
    private GeoDataService geoDataService;

    @MockitoBean
    private GeoDataDao geoDataDao;

    // ============================================================================
    // OWASP WSTG-ERRH-01: Exception propagation from DAO layer
    // GeoDataService non aggiunge try/catch: le eccezioni dal DAO devono propagarsi
    // al caller senza essere silenziate o trasformate in risposta vuota.
    // ============================================================================

    @Test
    @DisplayName("ottieniCoordinate - InternalServerErrorException from DAO must propagate to caller (WSTG-ERRH-01)")
    void testOttieniCoordinate_DaoThrowsInternalError_ShouldPropagate() {
        when(geoDataDao.ottieniCoordinate(anyString()))
                .thenThrow(new InternalServerErrorException(ErrorCode.EXTERNAL_SERVICE_ERROR));

        assertThrows(InternalServerErrorException.class,
                () -> geoDataService.ottieniCoordinate("Via Roma 1", "Genova"),
                "InternalServerErrorException from DAO must not be silenced by GeoDataService");
    }

    @Test
    @DisplayName("ottieniCoordinate - BadRequestException (ADDRESS_NOT_FOUND) from DAO must propagate (WSTG-ERRH-01)")
    void testOttieniCoordinate_DaoThrowsAddressNotFound_ShouldPropagate() {
        when(geoDataDao.ottieniCoordinate(anyString()))
                .thenThrow(new BadRequestException(ErrorCode.ADDRESS_NOT_FOUND));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> geoDataService.ottieniCoordinate("Indirizzo Inesistente 999", "ComuneInventato"),
                "BadRequestException (ADDRESS_NOT_FOUND) from DAO must reach the caller without modification");
        assertEquals(ErrorCode.ADDRESS_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("ottieniConteggioPuntiInteresse - BadRequestException (INVALID_CATEGORY) from DAO must propagate (WSTG-INPV-01)")
    void testOttieniConteggioPuntiInteresse_DaoThrowsInvalidCategory_ShouldPropagate() {
        when(geoDataDao.ottieniConteggioPuntiInteresse(anyDouble(), anyDouble(), anyInt(), anyList()))
                .thenThrow(new BadRequestException(ErrorCode.INVALID_CATEGORY));

        ConteggioPuntiInteresseRequest request = new ConteggioPuntiInteresseRequest(44.4, 9.2, 1000, List.of("categoria_sconosciuta"));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> geoDataService.ottieniConteggioPuntiInteresse(request),
                "BadRequestException (INVALID_CATEGORY) from DAO must reach the caller without modification");
        assertEquals(ErrorCode.INVALID_CATEGORY, ex.getErrorCode());
    }

    @Test
    @DisplayName("ottieniConteggioPuntiInteresse - InternalServerErrorException from DAO must propagate (WSTG-ERRH-01)")
    void testOttieniConteggioPuntiInteresse_DaoThrowsInternalError_ShouldPropagate() {
        when(geoDataDao.ottieniConteggioPuntiInteresse(anyDouble(), anyDouble(), anyInt(), anyList()))
                .thenThrow(new InternalServerErrorException(ErrorCode.EXTERNAL_SERVICE_ERROR));

        ConteggioPuntiInteresseRequest request = new ConteggioPuntiInteresseRequest(44.4, 9.2, 1000, List.of("parco"));

        assertThrows(InternalServerErrorException.class,
                () -> geoDataService.ottieniConteggioPuntiInteresse(request),
                "InternalServerErrorException from DAO must not be silenced by GeoDataService");
    }

    // ============================================================================
    // OWASP WSTG-INPV-01: Address concatenation integrity
    // ottieniCoordinate(indirizzo, comune) concatena i due parametri con ", "
    // e passa la stringa completa al DAO. Verifica che il DAO riceva esattamente
    // "indirizzo, comune" senza modifiche aggiuntive o troncamenti.
    // ============================================================================

    @Test
    @DisplayName("ottieniCoordinate - address is concatenated as 'indirizzo, comune' and forwarded unmodified (WSTG-INPV-01)")
    void testOttieniCoordinate_AddressConcatenation_ShouldForwardCorrectly() {
        Map<String, Double> coordinate = Map.of("latitudine", 44.4, "longitudine", 9.2);
        when(geoDataDao.ottieniCoordinate(anyString())).thenReturn(coordinate);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        geoDataService.ottieniCoordinate("Via Roma 1", "Genova");

        verify(geoDataDao).ottieniCoordinate(captor.capture());
        assertEquals("Via Roma 1, Genova", captor.getValue(),
                "GeoDataService must concatenate indirizzo and comune as 'indirizzo, comune' before calling the DAO");
    }
}
