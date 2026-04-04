package com.dietiestate25backend.security.dao;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.model.Utente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@DisplayName("UtentePostgres DAO Security Tests - WSTG-INPV-05, WSTG-CRYP-04")
class UtentePostgresDaoSecurityTests extends BaseIntegrationTest {

    @Autowired
    private UtenteDao utenteDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ============================================================================
    // OWASP WSTG-INPV-05: Testing for SQL Injection
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/07-Input_Validation_Testing/05-Testing_for_SQL_Injection
    // ============================================================================

    @Test
    @DisplayName("SQL Injection - findByEmail with OR injection must not bypass authentication")
    void testFindByEmail_WithOrInjection_ShouldNotReturnAnyUser() {
        assertThrows(EmptyResultDataAccessException.class,
                () -> utenteDao.findByEmail("' OR '1'='1"),
                "Parameterized query must treat injection payload as a literal string, not as SQL");
    }

    @Test
    @DisplayName("SQL Injection - findByEmail with UNION injection must not leak other users' data")
    void testFindByEmail_WithUnionInjection_ShouldNotLeakData() {
        assertThrows(EmptyResultDataAccessException.class,
                () -> utenteDao.findByEmail("x' UNION SELECT uid,email,password,role FROM utenti --"),
                "UNION injection must be neutralized by parameterized query");
    }

    @Test
    @DisplayName("SQL Injection - findEmailByUid with OR injection must not return any email")
    void testFindEmailByUid_WithOrInjection_ShouldNotReturnAnyEmail() {
        assertThrows(EmptyResultDataAccessException.class,
                () -> utenteDao.findEmailByUid("' OR '1'='1"),
                "Parameterized query must treat injection payload as a literal string, not as SQL");
    }

    // ============================================================================
    // OWASP WSTG-CRYP-04: Sensitive Data Integrity at Persistence Layer
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/09-Testing_for_Weak_Cryptography/04-Testing_for_Weak_Encryption
    // ============================================================================

    @Test
    @DisplayName("Sensitive Data Integrity - DAO must store password hash unchanged (hashing is service responsibility)")
    void testSave_ShouldStorePasswordHashUnchanged() {
        String uid = UUID.randomUUID().toString();
        String bcryptHash = "$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        Utente utente = new Utente(uid, uid + "@test.com", bcryptHash, "Cliente");

        boolean result = utenteDao.save(utente);

        assertTrue(result, "save must return true on successful insert");
        String stored = jdbcTemplate.queryForObject(
                "SELECT password FROM utenti WHERE uid = ?", String.class, uid);
        assertEquals(bcryptHash, stored,
                "DAO must persist the BCrypt hash exactly as provided - any transformation would break authentication");
    }
}
