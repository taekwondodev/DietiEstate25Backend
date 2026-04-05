package com.dietiestate25backend.security.dao;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.model.Offerta;
import com.dietiestate25backend.model.StatoOfferta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@DisplayName("OffertaPostgres DAO Security Tests - WSTG-INPV-05, WSTG-ATHZ-02, WSTG-INPV-01, WSTG-BUSL-07, WSTG-ERRH-01")
class OffertaPostgresDaoSecurityTests extends BaseIntegrationTest {

    @Autowired
    private OffertaDao offertaDao;

    // Pre-seeded test data (02_test_data.sql):
    //   offerta 1 → cliente-001, immobile 1 (agente-001)
    //   offerta 2 → cliente-002, immobile 2 (agente-001)
    //   offerta 3 → cliente-001, immobile 3 (gestore-001)

    // ============================================================================
    // OWASP WSTG-INPV-05: Testing for SQL Injection
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/07-Input_Validation_Testing/05-Testing_for_SQL_Injection
    // ============================================================================

    @Test
    @DisplayName("SQL Injection - OR injection in riepilogoOfferteCliente must return empty list")
    void testRiepilogoOfferteCliente_WithOrInjection_ShouldReturnEmptyList() {
        List<Offerta> result = offertaDao.riepilogoOfferteCliente("' OR '1'='1");

        assertTrue(result.isEmpty(),
                "OR injection in idCliente must be treated as a literal string and not return all offerte");
    }

    @Test
    @DisplayName("SQL Injection - OR injection in riepilogoOfferteUtenteAgenzia must return empty list")
    void testRiepilogoOfferteUtenteAgenzia_WithOrInjection_ShouldReturnEmptyList() {
        List<Offerta> result = offertaDao.riepilogoOfferteUteneAgenzia("' OR '1'='1");

        assertTrue(result.isEmpty(),
                "OR injection in idAgente must be treated as a literal string and not return all offerte");
    }

    // ============================================================================
    // OWASP WSTG-ATHZ-02: Testing for Bypassing Authorization Schema (Data Isolation)
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/05-Authorization_Testing/02-Testing_for_Bypassing_Authorization_Schema
    // ============================================================================

    @Test
    @DisplayName("Data Isolation - riepilogoOfferteCliente must not expose other clients' offers")
    void testRiepilogoOfferteCliente_ShouldNotExposeOtherClientsOffers() {
        List<Offerta> result = offertaDao.riepilogoOfferteCliente("uid-cliente-001");

        assertFalse(result.isEmpty(), "Client must be able to see his own offers");
        assertTrue(result.stream().allMatch(o -> "uid-cliente-001".equals(o.getIdCliente())),
                "riepilogoOfferteCliente must only return offers belonging to the requesting client");
        assertTrue(result.stream().noneMatch(o -> "uid-cliente-002".equals(o.getIdCliente())),
                "Offers of other clients must never be exposed through riepilogoOfferteCliente");
    }

    @Test
    @DisplayName("Data Isolation - riepilogoOfferteUtenteAgenzia must not expose other agents' offers")
    void testRiepilogoOfferteUtenteAgenzia_ShouldNotExposeOtherAgentsOffers() {
        List<Offerta> result = offertaDao.riepilogoOfferteUteneAgenzia("uid-agente-001");

        assertFalse(result.isEmpty(), "Agent must be able to see offers on his own properties");
        assertTrue(result.stream().allMatch(o -> "uid-agente-001".equals(o.getImmobile().getIdResponsabile())),
                "riepilogoOfferteUtenteAgenzia must only return offers on properties belonging to the requesting agent");
    }

    @Test
    @DisplayName("SQL Injection - UNION injection in riepilogoOfferteCliente must return empty list")
    void testRiepilogoOfferteCliente_WithUnionInjection_ShouldReturnEmptyList() {
        List<Offerta> result = offertaDao.riepilogoOfferteCliente(
                "x' UNION SELECT idOfferta,importo,stato,idCliente,idImmobile FROM offerta --");

        assertTrue(result.isEmpty(),
                "UNION injection in idCliente must be treated as a literal string and not leak offer data");
    }

    @Test
    @DisplayName("SQL Injection - UNION injection in riepilogoOfferteUtenteAgenzia must return empty list")
    void testRiepilogoOfferteUtenteAgenzia_WithUnionInjection_ShouldReturnEmptyList() {
        List<Offerta> result = offertaDao.riepilogoOfferteUteneAgenzia(
                "x' UNION SELECT idOfferta,importo,stato,idCliente,idImmobile FROM offerta --");

        assertTrue(result.isEmpty(),
                "UNION injection in idAgente must be treated as a literal string and not leak offer data");
    }

    @Test
    @DisplayName("SQL Injection - comment-based injection in riepilogoOfferteCliente must return empty list")
    void testRiepilogoOfferteCliente_WithCommentInjection_ShouldReturnEmptyList() {
        List<Offerta> result = offertaDao.riepilogoOfferteCliente("uid-cliente-001'--");

        assertTrue(result.isEmpty(),
                "Comment injection must be treated as a literal string and not truncate the WHERE clause");
    }

    @Test
    @DisplayName("SQL Injection - stacked query in riepilogoOfferteCliente must not execute DDL (WSTG-INPV-05)")
    void testRiepilogoOfferteCliente_WithStackedQueryInjection_ShouldReturnEmptyList() {
        List<Offerta> result = offertaDao.riepilogoOfferteCliente(
                "uid'; DROP TABLE offerta; --");

        assertTrue(result.isEmpty(),
                "Stacked query injection must be treated as a literal string and must not execute DDL statements");
    }

    @Test
    @DisplayName("SQL Injection - comment injection in riepilogoOfferteUtenteAgenzia must return empty list (WSTG-INPV-05)")
    void testRiepilogoOfferteUtenteAgenzia_WithCommentInjection_ShouldReturnEmptyList() {
        List<Offerta> result = offertaDao.riepilogoOfferteUteneAgenzia("uid-agente-001'--");

        assertTrue(result.isEmpty(),
                "Comment injection must not truncate the WHERE clause and return all offerte for the agent");
    }

    @Test
    @DisplayName("SQL Injection - stacked query in riepilogoOfferteUtenteAgenzia must not execute DDL (WSTG-INPV-05)")
    void testRiepilogoOfferteUtenteAgenzia_WithStackedQueryInjection_ShouldReturnEmptyList() {
        List<Offerta> result = offertaDao.riepilogoOfferteUteneAgenzia(
                "uid'; DROP TABLE offerta; --");

        assertTrue(result.isEmpty(),
                "Stacked query injection must be treated as a literal string and must not execute DDL statements");
    }

    // ============================================================================
    // OWASP WSTG-ATHZ-02: getOffertaById must return exactly the requested offer
    // Verifica che la query WHERE o.idOfferta = ? non possa essere aggirata per
    // restituire offerte di altri utenti tramite manipolazione dell'ID.
    // ============================================================================

    @Test
    @DisplayName("getOffertaById - valid ID must return only the requested offer, not others (WSTG-ATHZ-02)")
    void testGetOffertaById_ValidId_ShouldReturnOnlyRequestedOffer() {
        Offerta result = offertaDao.getOffertaById(1);

        assertNotNull(result, "A seeded offer with ID 1 must be retrievable");
        assertEquals(1, result.getIdOfferta(),
                "getOffertaById must return exactly the offer with the requested ID");
        assertEquals("uid-cliente-001", result.getIdCliente(),
                "The returned offer must belong to the correct client, not to another client");
    }

    // ============================================================================
    // OWASP WSTG-INPV-01: Referential integrity enforcement at DB level
    // Un attaccante che forgia un idImmobile o idCliente inesistente deve ricevere
    // un DataIntegrityViolationException, non un inserimento silenzioso.
    // ============================================================================

    @Test
    @DisplayName("salvaOfferta - non-existent idImmobile must throw DataIntegrityViolationException (WSTG-INPV-01)")
    void testSalvaOfferta_NonExistentIdImmobile_ShouldThrowDataIntegrityViolation() {
        assertThrows(DataIntegrityViolationException.class,
                () -> offertaDao.salvaOfferta(100000.0, StatoOfferta.IN_SOSPESO, "uid-cliente-001", Integer.MAX_VALUE),
                "Inserting an offer with a non-existent immobile ID must be rejected by the DB foreign key constraint");
    }

    @Test
    @DisplayName("salvaOfferta - non-existent idCliente must throw DataIntegrityViolationException (WSTG-INPV-01)")
    void testSalvaOfferta_NonExistentIdCliente_ShouldThrowDataIntegrityViolation() {
        assertThrows(DataIntegrityViolationException.class,
                () -> offertaDao.salvaOfferta(100000.0, StatoOfferta.IN_SOSPESO, "uid-non-existent-client", 1),
                "Inserting an offer with a non-existent client UID must be rejected by the DB foreign key constraint");
    }

    // ============================================================================
    // OWASP WSTG-BUSL-07: State transition persistence integrity at DB level
    // Verifica che aggiornaStatoOfferta scriva effettivamente il nuovo stato nel DB
    // e che getOffertaById lo rileva correttamente nella stessa transazione.
    // ============================================================================

    @Test
    @DisplayName("aggiornaStatoOfferta - state transition must be persisted correctly in DB (WSTG-BUSL-07)")
    void testAggiornaStatoOfferta_ShouldPersistStateTransitionCorrectly() {
        Offerta offertaAttuale = offertaDao.getOffertaById(1);
        assertEquals(StatoOfferta.IN_SOSPESO, offertaAttuale.getStato(),
                "Pre-condition: seeded offer 1 must be IN_SOSPESO");

        Offerta offertaAggiornata = new Offerta(1, offertaAttuale.getImporto(),
                StatoOfferta.ACCETTATA, offertaAttuale.getIdCliente(), offertaAttuale.getImmobile());
        boolean updated = offertaDao.aggiornaStatoOfferta(offertaAggiornata);

        assertTrue(updated, "aggiornaStatoOfferta must return true when the update affects at least one row");

        Offerta rilettura = offertaDao.getOffertaById(1);
        assertEquals(StatoOfferta.ACCETTATA, rilettura.getStato(),
                "After state transition, getOffertaById must reflect the new state — DB-level state machine integrity");
    }

    // ============================================================================
    // OWASP WSTG-ERRH-01: Error handling — missing resource must surface exception
    // Verifies that querying a non-existent offer by ID raises EmptyResultDataAccessException
    // instead of returning null (which would mask referencing errors).
    // ============================================================================

    @Test
    @DisplayName("getOffertaById - non-existent ID must throw EmptyResultDataAccessException (WSTG-ERRH-01)")
    void testGetOffertaById_NonExistentId_ShouldThrowEmptyResult() {
        assertThrows(EmptyResultDataAccessException.class,
                () -> offertaDao.getOffertaById(Integer.MAX_VALUE),
                "Querying a non-existent offer must not return null — it must throw EmptyResultDataAccessException");
    }
}