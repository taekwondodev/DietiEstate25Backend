package com.dietiestate25backend.security.dao;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@DisplayName("UtenteAgenziaPostgres DAO Security Tests - WSTG-INPV-05, WSTG-ATHZ-02")
class UtenteAgenziaPostgresDaoSecurityTests extends BaseIntegrationTest {

    @Autowired
    private UtenteAgenziaDao utenteAgenziaDao;

    // Pre-seeded test data (02_test_data.sql):
    //   uid-admin-001   → Admin,   agenzia 1  (in utenteagenzia)
    //   uid-cliente-001 → Cliente             (NOT in utenteagenzia)

    // ============================================================================
    // OWASP WSTG-INPV-05: Testing for SQL Injection
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/07-Input_Validation_Testing/05-Testing_for_SQL_Injection
    // ============================================================================

    @Test
    @DisplayName("SQL Injection - OR injection in getIdAgenzia must not return any agency ID")
    void testGetIdAgenzia_WithOrInjection_ShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> utenteAgenziaDao.getIdAgenzia("' OR '1'='1"),
                "OR injection must be treated as a literal string - no user should match");

        assertEquals(ErrorCode.ADMIN_NOT_FOUND, exception.getErrorCode(),
                "NotFoundException with ADMIN_NOT_FOUND must be thrown when no user matches");
    }

    @Test
    @DisplayName("SQL Injection - UNION injection in getIdAgenzia must not leak agency IDs")
    void testGetIdAgenzia_WithUnionInjection_ShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> utenteAgenziaDao.getIdAgenzia("x' UNION SELECT role, idagenzia FROM utenteagenzia --"),
                "UNION injection must be neutralized by parameterized query");

        assertEquals(ErrorCode.ADMIN_NOT_FOUND, exception.getErrorCode());
    }

    // ============================================================================
    // OWASP WSTG-ATHZ-02: Testing for Bypassing Authorization Schema
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/05-Authorization_Testing/02-Testing_for_Bypassing_Authorization_Schema
    // ============================================================================

    @Test
    @DisplayName("Authorization - Non-Admin role in getIdAgenzia must be rejected with INSUFFICIENT_PERMISSIONS")
    void testGetIdAgenzia_WithNonAdminRole_ShouldThrowUnauthorizedException() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> utenteAgenziaDao.getIdAgenzia("uid-cliente-001"),
                "DAO must reject non-Admin users before exposing agency information");

        assertEquals(ErrorCode.INSUFFICIENT_PERMISSIONS, exception.getErrorCode(),
                "Error code must be INSUFFICIENT_PERMISSIONS to prevent privilege escalation");
    }

    @Test
    @DisplayName("Authorization - Non-existent uid in getIdAgenzia must throw NotFoundException")
    void testGetIdAgenzia_WithNonExistentUid_ShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> utenteAgenziaDao.getIdAgenzia("non-existent-uid-000"),
                "Non-existent uid must throw NotFoundException without leaking DB internals");

        assertEquals(ErrorCode.ADMIN_NOT_FOUND, exception.getErrorCode());
    }
}