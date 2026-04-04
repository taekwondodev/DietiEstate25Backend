package com.dietiestate25backend.security.dao;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.model.Offerta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@DisplayName("OffertaPostgres DAO Security Tests - WSTG-INPV-05, WSTG-ATHZ-02")
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
}