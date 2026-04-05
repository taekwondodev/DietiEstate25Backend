package com.dietiestate25backend.security.dao;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.model.StatoVisita;
import com.dietiestate25backend.model.Visita;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@DisplayName("VisitaPostgres DAO Security Tests - WSTG-INPV-05, WSTG-ATHZ-02, WSTG-INPV-01, WSTG-BUSL-07, WSTG-ERRH-01")
class VisitaPostgresDaoSecurityTests extends BaseIntegrationTest {

    @Autowired
    private VisitaDao visitaDao;

    // Pre-seeded test data (02_test_data.sql):
    //   visita 1 → cliente-001, immobile 1 (agente-001)
    //   visita 2 → cliente-002, immobile 3 (gestore-001)
    //   visita 3 → cliente-001, immobile 2 (agente-001)

    // ============================================================================
    // OWASP WSTG-INPV-05: Testing for SQL Injection
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/07-Input_Validation_Testing/05-Testing_for_SQL_Injection
    // ============================================================================

    @Test
    @DisplayName("SQL Injection - OR injection in riepilogoVisiteCliente must return empty list")
    void testRiepilogoVisiteCliente_WithOrInjection_ShouldReturnEmptyList() {
        List<Visita> result = visitaDao.riepilogoVisiteCliente("' OR '1'='1");

        assertTrue(result.isEmpty(),
                "OR injection in idCliente must be treated as a literal string and not return all visite");
    }

    @Test
    @DisplayName("SQL Injection - OR injection in riepilogoVisiteUtenteAgenzia must return empty list")
    void testRiepilogoVisiteUtenteAgenzia_WithOrInjection_ShouldReturnEmptyList() {
        List<Visita> result = visitaDao.riepilogoVisiteUtenteAgenzia("' OR '1'='1");

        assertTrue(result.isEmpty(),
                "OR injection in idAgente must be treated as a literal string and not return all visite");
    }

    // ============================================================================
    // OWASP WSTG-ATHZ-02: Testing for Bypassing Authorization Schema (Data Isolation)
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/05-Authorization_Testing/02-Testing_for_Bypassing_Authorization_Schema
    // ============================================================================

    @Test
    @DisplayName("Data Isolation - riepilogoVisiteCliente must not expose other clients' visits")
    void testRiepilogoVisiteCliente_ShouldNotExposeOtherClientsVisits() {
        List<Visita> result = visitaDao.riepilogoVisiteCliente("uid-cliente-001");

        assertFalse(result.isEmpty(), "Client must be able to see his own visits");
        assertTrue(result.stream().allMatch(v -> "uid-cliente-001".equals(v.getIdCliente())),
                "riepilogoVisiteCliente must only return visits belonging to the requesting client");
        assertTrue(result.stream().noneMatch(v -> "uid-cliente-002".equals(v.getIdCliente())),
                "Visits of other clients must never be exposed through riepilogoVisiteCliente");
    }

    @Test
    @DisplayName("Data Isolation - riepilogoVisiteUtenteAgenzia must not expose other agents' visits")
    void testRiepilogoVisiteUtenteAgenzia_ShouldNotExposeOtherAgentsVisits() {
        List<Visita> result = visitaDao.riepilogoVisiteUtenteAgenzia("uid-agente-001");

        assertFalse(result.isEmpty(), "Agent must be able to see visits on his own properties");
        assertTrue(result.stream().allMatch(v -> "uid-agente-001".equals(v.getImmobile().getIdResponsabile())),
                "riepilogoVisiteUtenteAgenzia must only return visits for properties belonging to the requesting agent");
    }

    @Test
    @DisplayName("SQL Injection - UNION injection in riepilogoVisiteCliente must not leak all visite (WSTG-INPV-05)")
    void testRiepilogoVisiteCliente_WithUnionInjection_ShouldReturnEmptyList() {
        List<Visita> result = visitaDao.riepilogoVisiteCliente(
                "x' UNION SELECT idVisita,data,orario,stato,idCliente,idImmobile FROM visita --");

        assertTrue(result.isEmpty(),
                "UNION injection in idCliente must be treated as a literal string and not leak visit data");
    }

    @Test
    @DisplayName("SQL Injection - UNION injection in riepilogoVisiteUtenteAgenzia must return empty list (WSTG-INPV-05)")
    void testRiepilogoVisiteUtenteAgenzia_WithUnionInjection_ShouldReturnEmptyList() {
        List<Visita> result = visitaDao.riepilogoVisiteUtenteAgenzia(
                "x' UNION SELECT idVisita,data,orario,stato,idCliente,idImmobile FROM visita --");

        assertTrue(result.isEmpty(),
                "UNION injection in idAgente must be treated as a literal string and not leak visit data");
    }

    @Test
    @DisplayName("SQL Injection - comment injection in riepilogoVisiteCliente must not truncate WHERE clause (WSTG-INPV-05)")
    void testRiepilogoVisiteCliente_WithCommentInjection_ShouldReturnEmptyList() {
        List<Visita> result = visitaDao.riepilogoVisiteCliente("uid-cliente-001'--");

        assertTrue(result.isEmpty(),
                "Comment injection must not truncate the WHERE clause and return all visits for the client");
    }

    @Test
    @DisplayName("SQL Injection - comment injection in riepilogoVisiteUtenteAgenzia must not truncate WHERE clause (WSTG-INPV-05)")
    void testRiepilogoVisiteUtenteAgenzia_WithCommentInjection_ShouldReturnEmptyList() {
        List<Visita> result = visitaDao.riepilogoVisiteUtenteAgenzia("uid-agente-001'--");

        assertTrue(result.isEmpty(),
                "Comment injection must not truncate the WHERE clause and return all visits for the agent");
    }

    @Test
    @DisplayName("SQL Injection - stacked query in riepilogoVisiteCliente must not execute DDL (WSTG-INPV-05)")
    void testRiepilogoVisiteCliente_WithStackedQueryInjection_ShouldReturnEmptyList() {
        List<Visita> result = visitaDao.riepilogoVisiteCliente(
                "uid'; DROP TABLE visita; --");

        assertTrue(result.isEmpty(),
                "Stacked query injection must be treated as a literal string and must not execute DDL statements");
    }

    // ============================================================================
    // OWASP WSTG-ERRH-01: Missing resource must surface exception, not null
    // ============================================================================

    @Test
    @DisplayName("getVisitaById - non-existent ID must throw EmptyResultDataAccessException (WSTG-ERRH-01)")
    void testGetVisitaById_NonExistentId_ShouldThrowEmptyResult() {
        assertThrows(EmptyResultDataAccessException.class,
                () -> visitaDao.getVisitaById(Integer.MAX_VALUE),
                "Querying a non-existent visit must not return null — it must throw EmptyResultDataAccessException");
    }

    // ============================================================================
    // OWASP WSTG-INPV-01: Referential integrity enforcement at DB level
    // Un attaccante che forgia un idImmobile inesistente deve ricevere
    // un DataIntegrityViolationException, non un inserimento silenzioso.
    // ============================================================================

    @Test
    @DisplayName("salva - non-existent idImmobile must throw DataIntegrityViolationException (WSTG-INPV-01)")
    void testSalva_NonExistentIdImmobile_ShouldThrowDataIntegrityViolation() {
        assertThrows(DataIntegrityViolationException.class,
                () -> visitaDao.salva(
                        Date.valueOf(LocalDate.now().plusDays(1)),
                        Time.valueOf(LocalTime.of(10, 0)),
                        StatoVisita.IN_SOSPESO,
                        "uid-cliente-001",
                        Integer.MAX_VALUE),
                "Inserting a visit with a non-existent immobile ID must be rejected by the DB foreign key constraint");
    }

    // ============================================================================
    // OWASP WSTG-BUSL-07: State transition persistence integrity at DB level
    // Verifica che aggiornaStato scriva effettivamente il nuovo stato nel DB
    // e che getVisitaById lo rileva correttamente nella stessa transazione.
    // ============================================================================

    @Test
    @DisplayName("aggiornaStato - state transition must be persisted correctly in DB (WSTG-BUSL-07)")
    void testAggiornaStato_ShouldPersistStateTransitionCorrectly() {
        Visita visitaAttuale = visitaDao.getVisitaById(2);
        assertEquals(StatoVisita.IN_SOSPESO, visitaAttuale.getStato(),
                "Pre-condition: seeded visit 2 must be IN_SOSPESO");

        Visita visitaAggiornata = new Visita(2, visitaAttuale.getDataVisita(), visitaAttuale.getOraVisita(),
                StatoVisita.CONFERMATA, visitaAttuale.getIdCliente(), visitaAttuale.getImmobile());
        boolean updated = visitaDao.aggiornaStato(visitaAggiornata);

        assertTrue(updated, "aggiornaStato must return true when the update affects at least one row");

        Visita rilettura = visitaDao.getVisitaById(2);
        assertEquals(StatoVisita.CONFERMATA, rilettura.getStato(),
                "After state transition, getVisitaById must reflect the new state — DB-level state machine integrity");
    }
}