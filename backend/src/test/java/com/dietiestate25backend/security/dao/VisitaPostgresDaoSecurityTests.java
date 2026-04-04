package com.dietiestate25backend.security.dao;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.model.Visita;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@DisplayName("VisitaPostgres DAO Security Tests - WSTG-INPV-05, WSTG-ATHZ-02")
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
}