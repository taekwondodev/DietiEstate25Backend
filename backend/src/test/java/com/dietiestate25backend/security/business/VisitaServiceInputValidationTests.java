package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.dto.requests.AggiornaVisitaRequest;
import com.dietiestate25backend.dto.requests.PrenotaVisitaRequest;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.DatabaseErrorException;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.StatoVisita;
import com.dietiestate25backend.model.Visita;
import com.dietiestate25backend.service.VisitaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * VisitaService Input Validation Security Tests
 *
 * Verifica che il service applichi correttamente i controlli di input
 * prima di raggiungere il layer DAO, secondo le linee guida OWASP:
 *
 * WSTG-INPV-01: Testing for Reflected Cross Site Scripting
 * WSTG-INPV-05: Testing for SQL Injection
 * WSTG-BUSL-07: Testing for Circumventing Workflows
 * WSTG-ERRH-01: Testing for Improper Error Handling
 */
@DisplayName("VisitaService Input Validation Security Tests - WSTG-INPV-01, WSTG-BUSL-07, WSTG-ERRH-01")
class VisitaServiceInputValidationTests extends BaseIntegrationTest {

    @Autowired
    private VisitaService visitaService;

    @MockitoBean
    private VisitaDao visitaDao;

    private void mockJwtUser(String uid, String role) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", uid)
                .claim("role", role)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Immobile buildTestImmobile(String idResponsabile) {
        return Immobile.builder()
                .idImmobile(1).indirizzo("Via Test")
                .latitudine(43.7).longitudine(10.4)
                .descrizione("Test immobile").nStanze(3).dimensione(80).nBagni(1)
                .urlFoto("image.jpg").idResponsabile(idResponsabile)
                .prezzo(100000.0).tipologia("Appartamento").comune("Genova")
                .piano(1).hasAscensore(false).hasBalcone(false).build();
    }

    // ============================================================================
    // OWASP WSTG-INPV-01: Identity field tampering — null/blank UID in prenotaVisita
    // ============================================================================

    @Test
    @DisplayName("prenotaVisita - null uidCliente must be rejected before reaching DAO (WSTG-INPV-01)")
    void testPrenotaVisita_NullUidCliente_ShouldThrowBadRequest() {
        PrenotaVisitaRequest request = new PrenotaVisitaRequest(1, LocalDate.now().plusDays(1), LocalTime.of(10, 0));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.prenotaVisita(request, null));
        assertEquals(ErrorCode.INVALID_CLIENT_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("prenotaVisita - whitespace-only uidCliente must be rejected before reaching DAO (WSTG-INPV-01)")
    void testPrenotaVisita_BlankUidCliente_ShouldThrowBadRequest() {
        PrenotaVisitaRequest request = new PrenotaVisitaRequest(1, LocalDate.now().plusDays(1), LocalTime.of(10, 0));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.prenotaVisita(request, "   "));
        assertEquals(ErrorCode.INVALID_CLIENT_ID, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-BUSL-07: Business logic bypass — out-of-hours booking
    // Attacker forges a request with a time outside the 08:00-18:00 window
    // ============================================================================

    @Test
    @DisplayName("prenotaVisita - visit at 07:59 (before business hours) must be rejected (WSTG-BUSL-07)")
    void testPrenotaVisita_TimeBeforeWorkHours_ShouldThrowBadRequest() {
        PrenotaVisitaRequest request = new PrenotaVisitaRequest(1, LocalDate.now().plusDays(1), LocalTime.of(7, 59));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.prenotaVisita(request, "client-uid"));
        assertEquals(ErrorCode.INVALID_VISIT_TIME, ex.getErrorCode());
    }

    @Test
    @DisplayName("prenotaVisita - visit at 18:01 (after business hours) must be rejected (WSTG-BUSL-07)")
    void testPrenotaVisita_TimeAfterWorkHours_ShouldThrowBadRequest() {
        PrenotaVisitaRequest request = new PrenotaVisitaRequest(1, LocalDate.now().plusDays(1), LocalTime.of(18, 1));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.prenotaVisita(request, "client-uid"));
        assertEquals(ErrorCode.INVALID_VISIT_TIME, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-INPV-01: Referential integrity attacks — forged FK values
    // Attacker supplies a non-existent immobile or client UID; the service must
    // surface a clean NotFoundException instead of leaking a DB error.
    // ============================================================================

    @Test
    @DisplayName("prenotaVisita - FK violation on immobile must produce NotFoundException (WSTG-INPV-01)")
    void testPrenotaVisita_ImmobileFKViolation_ShouldThrowNotFoundException() {
        PrenotaVisitaRequest request = new PrenotaVisitaRequest(99999, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        when(visitaDao.salva(any(), any(), any(), anyString(), anyInt()))
                .thenThrow(new DataIntegrityViolationException("foreign key constraint on immobile violated"));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> visitaService.prenotaVisita(request, "client-uid"));
        assertEquals(ErrorCode.IMMOBILE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("prenotaVisita - FK violation on idcliente must produce NotFoundException (WSTG-INPV-01)")
    void testPrenotaVisita_ClienteFKViolation_ShouldThrowNotFoundException() {
        PrenotaVisitaRequest request = new PrenotaVisitaRequest(1, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        when(visitaDao.salva(any(), any(), any(), anyString(), anyInt()))
                .thenThrow(new DataIntegrityViolationException("foreign key constraint on idcliente violated"));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> visitaService.prenotaVisita(request, "non-existent-client-uid"));
        assertEquals(ErrorCode.CLIENT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("prenotaVisita - DAO write failure must surface DatabaseErrorException, not raw DAO error (WSTG-ERRH-01)")
    void testPrenotaVisita_DaoReturnsFalse_ShouldThrowDatabaseError() {
        PrenotaVisitaRequest request = new PrenotaVisitaRequest(1, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        when(visitaDao.salva(any(), any(), any(), anyString(), anyInt())).thenReturn(false);

        assertThrows(DatabaseErrorException.class,
                () -> visitaService.prenotaVisita(request, "client-uid"));
    }

    // ============================================================================
    // OWASP WSTG-INPV-01: Null/blank guard on listing endpoints
    // Prevents enumeration of all visits via a forged/missing principal
    // ============================================================================

    @Test
    @DisplayName("riepilogoVisiteCliente - null idCliente must be rejected before DAO call (WSTG-INPV-01)")
    void testRiepilogoVisiteCliente_NullId_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.riepilogoVisiteCliente(null));
        assertEquals(ErrorCode.INVALID_CLIENT_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("riepilogoVisiteCliente - whitespace idCliente must be rejected before DAO call (WSTG-INPV-01)")
    void testRiepilogoVisiteCliente_BlankId_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.riepilogoVisiteCliente("   "));
        assertEquals(ErrorCode.INVALID_CLIENT_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("riepilogoVisiteUtenteAgenzia - null idAgente must be rejected before DAO call (WSTG-INPV-01)")
    void testRiepilogoVisiteUtenteAgenzia_NullId_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.riepilogoVisiteUtenteAgenzia(null));
        assertEquals(ErrorCode.INVALID_USER_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("riepilogoVisiteUtenteAgenzia - whitespace idAgente must be rejected before DAO call (WSTG-INPV-01)")
    void testRiepilogoVisiteUtenteAgenzia_BlankId_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.riepilogoVisiteUtenteAgenzia("   "));
        assertEquals(ErrorCode.INVALID_USER_ID, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-INPV-01 / WSTG-INPV-05: Identity and stato injection in aggiornaStatoVisita
    // ============================================================================

    @Test
    @DisplayName("aggiornaStatoVisita - null uidUtente must be rejected before DAO call (WSTG-INPV-01)")
    void testAggiornaStatoVisita_NullUidUtente_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(1, "Confermata"), null));
        assertEquals(ErrorCode.INVALID_USER_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("aggiornaStatoVisita - whitespace uidUtente must be rejected before DAO call (WSTG-INPV-01)")
    void testAggiornaStatoVisita_BlankUidUtente_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(1, "Confermata"), "   "));
        assertEquals(ErrorCode.INVALID_USER_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("aggiornaStatoVisita - SQL injection in stato field must be rejected as INVALID_STATUS (WSTG-INPV-05)")
    void testAggiornaStatoVisita_SqlInjectionInStato_ShouldThrowBadRequest() {
        AggiornaVisitaRequest request = new AggiornaVisitaRequest(1, "'; DROP TABLE visita; --");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(request, "client-uid"));
        assertEquals(ErrorCode.INVALID_STATUS, ex.getErrorCode());
    }

    @Test
    @DisplayName("aggiornaStatoVisita - arbitrary string in stato must be rejected as INVALID_STATUS (WSTG-INPV-01)")
    void testAggiornaStatoVisita_ArbitraryStato_ShouldThrowBadRequest() {
        AggiornaVisitaRequest request = new AggiornaVisitaRequest(1, "INVALID_STATE_TAMPERING");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(request, "client-uid"));
        assertEquals(ErrorCode.INVALID_STATUS, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-ERRH-01: Error handling — DAO failures must not leak internals
    // ============================================================================

    @Test
    @DisplayName("aggiornaStatoVisita - DAO update failure must produce DatabaseErrorException (WSTG-ERRH-01)")
    void testAggiornaStatoVisita_DaoUpdateReturnsFalse_ShouldThrowDatabaseError() {
        mockJwtUser("client1", "Cliente");
        Immobile immobile = buildTestImmobile("agente1");
        Visita visitaInSospeso = new Visita(10, Date.valueOf("2026-05-01"), Time.valueOf("10:00:00"),
                StatoVisita.IN_SOSPESO, "client1", immobile);

        when(visitaDao.getVisitaById(10)).thenReturn(visitaInSospeso);
        when(visitaDao.aggiornaStato(any())).thenReturn(false);

        assertThrows(DatabaseErrorException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(10, "Confermata"), "client1"));
    }

    @Test
    @DisplayName("aggiornaStatoVisita - EmptyResultDataAccessException must map to NotFoundException, not leak stack (WSTG-ERRH-01)")
    void testAggiornaStatoVisita_EmptyResultException_ShouldThrowNotFoundException() {
        mockJwtUser("client1", "Cliente");
        when(visitaDao.getVisitaById(anyInt())).thenThrow(new EmptyResultDataAccessException(1));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(9999, "Confermata"), "client1"));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }
}
