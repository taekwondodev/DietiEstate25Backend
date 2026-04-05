package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.dto.requests.AggiornaOffertaRequest;
import com.dietiestate25backend.dto.requests.CreaOffertaRequest;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.DatabaseErrorException;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Offerta;
import com.dietiestate25backend.model.StatoOfferta;
import com.dietiestate25backend.service.OffertaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * OffertaService Input Validation Security Tests
 *
 * Verifica che OffertaService applichi i controlli di input e le regole
 * di business prima di raggiungere il layer DAO, secondo OWASP:
 *
 * WSTG-INPV-01: Identity field tampering (null/blank UID guards, invalid status strings)
 * WSTG-INPV-05: SQL injection in stato field caught at enum parse layer
 * WSTG-BUSL-07: State machine bypass (invalid transitions: RIFIUTATA→ACCETTATA, ACCETTATA→RIFIUTATA)
 * WSTG-ATHZ-02: Horizontal privilege escalation (cliente updates another client's offer)
 * WSTG-ERRH-01: DAO failure wrapping without information leakage
 */
@DisplayName("OffertaService Input Validation Security Tests - WSTG-INPV-01, WSTG-INPV-05, WSTG-BUSL-07, WSTG-ATHZ-02, WSTG-ERRH-01")
class OffertaServiceInputValidationTests extends BaseIntegrationTest {

    @Autowired
    private OffertaService offertaService;

    @MockitoBean
    private OffertaDao offertaDao;

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
                .idImmobile(1).indirizzo("Via Test 1")
                .latitudine(40.8).longitudine(14.2)
                .descrizione("Test immobile").nStanze(3).dimensione(80).nBagni(1)
                .urlFoto("image.jpg").idResponsabile(idResponsabile)
                .prezzo(200000.0).tipologia("Appartamento").comune("Napoli")
                .piano(2).hasAscensore(true).hasBalcone(false).build();
    }

    private Offerta buildOfferta(int id, StatoOfferta stato, String idCliente, Immobile immobile) {
        return new Offerta(id, 150000.0, stato, idCliente, immobile);
    }

    // ============================================================================
    // OWASP WSTG-INPV-01: Identity field tampering — null/blank UID guards
    // ============================================================================

    @Test
    @DisplayName("aggiungiOfferta - null uidCliente must be rejected before DAO call (WSTG-INPV-01)")
    void testAggiungiOfferta_NullUidCliente_ShouldThrowBadRequest() {
        CreaOffertaRequest request = new CreaOffertaRequest(150000.0, 1);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.aggiungiOfferta(request, null));
        assertEquals(ErrorCode.INVALID_CLIENT_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("aggiungiOfferta - blank uidCliente must be rejected before DAO call (WSTG-INPV-01)")
    void testAggiungiOfferta_BlankUidCliente_ShouldThrowBadRequest() {
        CreaOffertaRequest request = new CreaOffertaRequest(150000.0, 1);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.aggiungiOfferta(request, "   "));
        assertEquals(ErrorCode.INVALID_CLIENT_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("aggiornaStatoOfferta - null uidUtente must be rejected before DAO call (WSTG-INPV-01)")
    void testAggiornaStatoOfferta_NullUidUtente_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(1, "Accettata"), null));
        assertEquals(ErrorCode.INVALID_USER_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("aggiornaStatoOfferta - blank uidUtente must be rejected before DAO call (WSTG-INPV-01)")
    void testAggiornaStatoOfferta_BlankUidUtente_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(1, "Accettata"), "   "));
        assertEquals(ErrorCode.INVALID_USER_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("riepilogoOfferteCliente - null idCliente must be rejected before DAO call (WSTG-INPV-01)")
    void testRiepilogoOfferteCliente_NullId_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.riepilogoOfferteCliente(null));
        assertEquals(ErrorCode.INVALID_CLIENT_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("riepilogoOfferteCliente - blank idCliente must be rejected before DAO call (WSTG-INPV-01)")
    void testRiepilogoOfferteCliente_BlankId_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.riepilogoOfferteCliente("   "));
        assertEquals(ErrorCode.INVALID_CLIENT_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("riepilogoOfferteUtenteAgenzia - null idAgente must be rejected before DAO call (WSTG-INPV-01)")
    void testRiepilogoOfferteUtenteAgenzia_NullId_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.riepilogoOfferteUtenteAgenzia(null));
        assertEquals(ErrorCode.INVALID_USER_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("riepilogoOfferteUtenteAgenzia - blank idAgente must be rejected before DAO call (WSTG-INPV-01)")
    void testRiepilogoOfferteUtenteAgenzia_BlankId_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.riepilogoOfferteUtenteAgenzia("   "));
        assertEquals(ErrorCode.INVALID_USER_ID, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-INPV-05: SQL injection attempt in stato field
    // StatoOfferta.fromString() acts as a strict allowlist: only "Accettata",
    // "Rifiutata", "In Sospeso" are valid. Any injection payload is rejected
    // before the value can reach the DAO layer.
    // ============================================================================

    @Test
    @DisplayName("aggiornaStatoOfferta - SQL injection in stato must be rejected as INVALID_STATUS (WSTG-INPV-05)")
    void testAggiornaStatoOfferta_SqlInjectionInStato_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(1, "'; UPDATE offerta SET stato='Accettata' WHERE '1'='1"),
                        "uid-utente"));
        assertEquals(ErrorCode.INVALID_STATUS, ex.getErrorCode());
    }

    @Test
    @DisplayName("aggiornaStatoOfferta - arbitrary invalid stato string must be rejected as INVALID_STATUS (WSTG-INPV-01)")
    void testAggiornaStatoOfferta_InvalidStatoString_ShouldThrowBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(1, "STATO_NON_ESISTENTE"),
                        "uid-utente"));
        assertEquals(ErrorCode.INVALID_STATUS, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-BUSL-07: State machine bypass — invalid transition attempts
    // Un attaccante non deve poter forzare transizioni non consentite nella
    // macchina a stati delle offerte (es. RIFIUTATA → ACCETTATA, ACCETTATA → qualsiasi).
    // ============================================================================

    @Test
    @DisplayName("aggiornaStatoOfferta - RIFIUTATA→ACCETTATA transition must be rejected as INVALID_OFFERTA_STATUS (WSTG-BUSL-07)")
    void testAggiornaStatoOfferta_RifiutataToAccettata_ShouldThrowBadRequest() {
        mockJwtUser("agente-uid", "AgenteImmobiliare");
        Immobile immobile = buildTestImmobile("agente-uid");
        when(offertaDao.getOffertaById(1))
                .thenReturn(buildOfferta(1, StatoOfferta.RIFIUTATA, "client-uid", immobile));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(1, "Accettata"), "agente-uid"));
        assertEquals(ErrorCode.INVALID_OFFERTA_STATUS, ex.getErrorCode());
    }

    @Test
    @DisplayName("aggiornaStatoOfferta - ACCETTATA→RIFIUTATA transition must be rejected as INVALID_OFFERTA_STATUS (WSTG-BUSL-07)")
    void testAggiornaStatoOfferta_AccettataToRifiutata_ShouldThrowBadRequest() {
        mockJwtUser("agente-uid", "AgenteImmobiliare");
        Immobile immobile = buildTestImmobile("agente-uid");
        when(offertaDao.getOffertaById(1))
                .thenReturn(buildOfferta(1, StatoOfferta.ACCETTATA, "client-uid", immobile));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(1, "Rifiutata"), "agente-uid"));
        assertEquals(ErrorCode.INVALID_OFFERTA_STATUS, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-ATHZ-02: Horizontal privilege escalation
    // Un cliente non deve poter modificare l'offerta di un altro cliente
    // passando il proprio UID ma un idOfferta appartenente ad altri.
    // ============================================================================

    @Test
    @DisplayName("aggiornaStatoOfferta - cliente updating another client's offer must be rejected as UNAUTHORIZED (WSTG-ATHZ-02)")
    void testAggiornaStatoOfferta_ClienteUpdatesOtherClientOffer_ShouldThrowUnauthorized() {
        mockJwtUser("attacker-uid", "Cliente");
        Immobile immobile = buildTestImmobile("agente-uid");
        when(offertaDao.getOffertaById(1))
                .thenReturn(buildOfferta(1, StatoOfferta.IN_SOSPESO, "victim-uid", immobile));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(1, "Rifiutata"), "attacker-uid"));
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-ERRH-01: Error handling — DAO failures must not leak internals
    // ============================================================================

    @Test
    @DisplayName("aggiornaStatoOfferta - DAO update failure must produce DatabaseErrorException (WSTG-ERRH-01)")
    void testAggiornaStatoOfferta_DaoUpdateReturnsFalse_ShouldThrowDatabaseError() {
        mockJwtUser("agente-uid", "AgenteImmobiliare");
        Immobile immobile = buildTestImmobile("agente-uid");
        when(offertaDao.getOffertaById(1))
                .thenReturn(buildOfferta(1, StatoOfferta.IN_SOSPESO, "client-uid", immobile));
        when(offertaDao.aggiornaStatoOfferta(any())).thenReturn(false);

        assertThrows(DatabaseErrorException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(1, "Accettata"), "agente-uid"));
    }

    @Test
    @DisplayName("aggiornaStatoOfferta - non-existent offerta must produce NotFoundException (WSTG-ERRH-01)")
    void testAggiornaStatoOfferta_NonExistentOfferta_ShouldThrowNotFoundException() {
        mockJwtUser("agente-uid", "AgenteImmobiliare");
        when(offertaDao.getOffertaById(anyInt()))
                .thenThrow(new org.springframework.dao.EmptyResultDataAccessException(1));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(99999, "Accettata"), "agente-uid"));
        assertEquals(ErrorCode.OFFERTA_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("aggiungiOfferta - FK violation on idImmobile must produce NotFoundException IMMOBILE_NOT_FOUND (WSTG-ERRH-01)")
    void testAggiungiOfferta_ImmobileFKViolation_ShouldThrowNotFound() {
        when(offertaDao.salvaOfferta(anyDouble(), any(), anyString(), anyInt()))
                .thenThrow(new DataIntegrityViolationException("foreign key constraint on idimmobile violated"));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> offertaService.aggiungiOfferta(new CreaOffertaRequest(150000.0, 99999), "client-uid"));
        assertEquals(ErrorCode.IMMOBILE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("aggiungiOfferta - FK violation on idCliente must produce NotFoundException CLIENT_NOT_FOUND (WSTG-ERRH-01)")
    void testAggiungiOfferta_ClienteFKViolation_ShouldThrowNotFound() {
        when(offertaDao.salvaOfferta(anyDouble(), any(), anyString(), anyInt()))
                .thenThrow(new DataIntegrityViolationException("foreign key constraint on idcliente violated"));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> offertaService.aggiungiOfferta(new CreaOffertaRequest(150000.0, 1), "non-existent-client"));
        assertEquals(ErrorCode.CLIENT_NOT_FOUND, ex.getErrorCode());
    }
}