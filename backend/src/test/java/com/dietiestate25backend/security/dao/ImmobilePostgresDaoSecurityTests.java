package com.dietiestate25backend.security.dao;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.model.Immobile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@DisplayName("ImmobilePostgres DAO Security Tests - WSTG-INPV-05, WSTG-ATHZ-02")
class ImmobilePostgresDaoSecurityTests extends BaseIntegrationTest {

    @Autowired
    private ImmobileDao immobileDao;

    // Pre-seeded test data (02_test_data.sql):
    //   uid-agente-001 → immobili 1 (Roma), 2 (Milano)
    //   uid-agente-002 → immobile 4 (Torino)
    //   uid-gestore-001 → immobile 3 (Napoli)

    // ============================================================================
    // OWASP WSTG-INPV-05: Testing for SQL Injection
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/07-Input_Validation_Testing/05-Testing_for_SQL_Injection
    // ============================================================================

    @Test
    @DisplayName("SQL Injection - OR injection in 'comune' filter must return empty list")
    void testCercaImmobili_WithOrInjectionInComune_ShouldReturnEmptyList() {
        Map<String, Object> filters = Map.of("comune", "' OR '1'='1");

        List<Immobile> result = immobileDao.cercaImmobiliConFiltri(filters, 0, 10);

        assertTrue(result.isEmpty(),
                "OR injection in 'comune' must be treated as a literal string and not return all rows");
    }

    @Test
    @DisplayName("SQL Injection - UNION injection in 'comune' filter must not leak all immobili")
    void testCercaImmobili_WithUnionInjectionInComune_ShouldReturnEmptyList() {
        Map<String, Object> filters = Map.of("comune", "x' UNION SELECT * FROM immobile --");

        List<Immobile> result = immobileDao.cercaImmobiliConFiltri(filters, 0, 10);

        assertTrue(result.isEmpty(),
                "UNION injection must be blocked by parameterized query");
    }

    @Test
    @DisplayName("SQL Injection - OR injection in immobiliPersonali uid must return empty list")
    void testImmobiliPersonali_WithOrInjection_ShouldReturnEmptyList() {
        List<Immobile> result = immobileDao.immobiliPersonali("' OR '1'='1");

        assertTrue(result.isEmpty(),
                "OR injection in uidResponsabile must return empty list, not all immobili");
    }

    // ============================================================================
    // OWASP WSTG-ATHZ-02: Testing for Bypassing Authorization Schema (Data Isolation)
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/05-Authorization_Testing/02-Testing_for_Bypassing_Authorization_Schema
    // ============================================================================

    @Test
    @DisplayName("Data Isolation - immobiliPersonali must return only properties of the requesting agent")
    void testImmobiliPersonali_ShouldReturnOnlyOwnerProperties() {
        List<Immobile> result = immobileDao.immobiliPersonali("uid-agente-001");

        assertFalse(result.isEmpty(), "Agent must be able to see his own properties");
        assertTrue(result.stream().allMatch(i -> "uid-agente-001".equals(i.getIdResponsabile())),
                "immobiliPersonali must only return properties belonging to the requesting agent");
        assertTrue(result.stream().noneMatch(i -> "uid-agente-002".equals(i.getIdResponsabile())),
                "Properties of other agents must not be accessible through immobiliPersonali");
    }
}
