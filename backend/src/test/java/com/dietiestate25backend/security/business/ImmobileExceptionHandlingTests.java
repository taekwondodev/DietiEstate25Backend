package com.dietiestate25backend.security.business;

import com.dietiestate25backend.TestConfiguration;
import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.dto.requests.CreaImmobileRequest;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.ConflictException;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.service.GeoDataService;
import com.dietiestate25backend.service.ImmobileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Immobile Exception Handling Tests
 *
 * Business Logic Security - Phase 4
 *
 * Verifica che il service lanci eccezioni appropriate senza leakare
 * informazioni sensibili tramite stack trace o messaggi dettagliati.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Immobile Exception Handling Tests - Business Logic Security")
class ImmobileExceptionHandlingTests {

    @Autowired
    private ImmobileService immobileService;

    @MockitoBean
    private ImmobileDao immobileDao;

    @MockitoBean
    private GeoDataService geoDataService;

    /**
     * DAO lancia DataAccessException durante ricerca immobili.
     *
     * Precondizione: ImmobileDao.cercaImmobiliConFiltri() lancia DataAccessException
     * Azione: cercaImmobili() è chiamato
     * Aspettativa: InternalServerErrorException lanciato con messaggio generico
     */
    @Test
    @DisplayName("Search immobili - DAO throws DataAccessException - SHOULD wrap without leaking")
    @WithMockUser(username = "user1", roles = "Cliente")
    void testCercaImmobili_DAOThrowsDataAccessException_ShouldWrapGeneric() {
        when(immobileDao.cercaImmobiliConFiltri(anyMap(), anyInt(), anyInt()))
                .thenThrow(new DataAccessException("Database connection lost") {});

        assertThrows(
                InternalServerErrorException.class,
                () -> immobileService.cercaImmobili("Genova", null, null, null, null, null, 0, 10),
                "DataAccessException dovrebbe essere wrapped in InternalServerErrorException"
        );
    }

    /**
     * GeoDataService non riesce a ottenere coordinate.
     *
     * Precondizione: geoDataService.ottieniCoordinate() ritorna null
     * Azione: creaImmobile() è chiamato
     * Aspettativa: BadRequestException lanciato con messaggio generico
     */
    @Test
    @DisplayName("Create immobile - Geo service returns null - SHOULD throw BadRequestException")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testCreaImmobile_GeoServiceReturnsNull_ShouldThrowBadRequest() {
        CreaImmobileRequest request = new CreaImmobileRequest(
                "image.jpg",
                "Piccolo monolocale",
                300000.0,
                50.0,
                1,
                1,
                "Monolocale",
                "Via Torino 20",
                "Torino",
                5,
                false,
                false
        );

        when(geoDataService.ottieniCoordinate("Via Roma 10", "Genova"))
                .thenReturn(null);

        assertThrows(
                BadRequestException.class,
                () -> immobileService.creaImmobile(request, "agente1"),
                "Null coordinate dovrebbe lanciare BadRequestException"
        );
    }

    /**
     * DAO rifiuta l'immobile per violazione di constraint.
     *
     * Precondizione: immobileDao.creaImmobile() lancia DataIntegrityViolationException
     * Azione: creaImmobile() è chiamato
     * Aspettativa: ConflictException lanciato
     */
    @Test
    @DisplayName("Create immobile - DAO constraint violation - SHOULD throw ConflictException")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testCreaImmobile_DAOConstraintViolation_ShouldThrowConflict() {
        CreaImmobileRequest request = new CreaImmobileRequest(
                "image.jpg",
                "Piccolo monolocale",
                300000.0,
                50.0,
                1,
                1,
                "Monolocale",
                "Via Torino 20",
                "Torino",
                5,
                false,
                false
        );

        when(geoDataService.ottieniCoordinate("Via Milano 5", "Milano"))
                .thenReturn(java.util.Map.of("latitudine", 45.5, "longitudine", 9.2));

        when(immobileDao.creaImmobile(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DataIntegrityViolationException("Foreign key constraint violated"));

        assertThrows(
                ConflictException.class,
                () -> immobileService.creaImmobile(request, "agente1"),
                "Constraint violation dovrebbe essere wrapped in ConflictException"
        );
    }

    /**
     * DAO lancia DataAccessException durante creazione immobile.
     *
     * Precondizione: immobileDao.creaImmobile() lancia DataAccessException
     * Azione: creaImmobile() è chiamato
     * Aspettativa: InternalServerErrorException lanciato senza leakage
     */
    @Test
    @DisplayName("Create immobile - DAO throws DataAccessException - SHOULD wrap without leaking")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testCreaImmobile_DAOThrowsDataAccessException_ShouldWrapGeneric() {
        CreaImmobileRequest request = new CreaImmobileRequest(
                "image.jpg",
                "Piccolo monolocale",
                300000.0,
                50.0,
                1,
                1,
                "Monolocale",
                "Via Torino 20",
                "Torino",
                5,
                false,
                false
        );

        when(geoDataService.ottieniCoordinate("Via Torino 20", "Torino"))
                .thenReturn(java.util.Map.of("latitudine", 45.1, "longitudine", 7.6));

        when(immobileDao.creaImmobile(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DataAccessException("Database unreachable") {});

        assertThrows(
                InternalServerErrorException.class,
                () -> immobileService.creaImmobile(request, "agente1"),
                "DataAccessException dovrebbe essere wrapped generically"
        );
    }

    /**
     * DAO lancia DataAccessException durante recupero immobili personali.
     *
     * Precondizione: immobileDao.immobiliPersonali() lancia DataAccessException
     * Azione: immobiliPersonali() è chiamato
     * Aspettativa: InternalServerErrorException lanciato senza leakage
     */
    @Test
    @DisplayName("Get personal immobili - DAO throws DataAccessException - SHOULD wrap without leaking")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testImmobiliPersonali_DAOThrowsDataAccessException_ShouldWrapGeneric() {
        when(immobileDao.immobiliPersonali("agente1"))
                .thenThrow(new DataAccessException("Query timeout") {});

        assertThrows(
                InternalServerErrorException.class,
                () -> immobileService.immobiliPersonali("agente1"),
                "DataAccessException dovrebbe essere wrapped generically"
        );
    }
}
