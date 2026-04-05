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

    @Test
    @DisplayName("SQL Injection - comment-based injection in findByEmail must not bypass authentication (WSTG-INPV-05)")
    void testFindByEmail_WithCommentInjection_ShouldNotBypassAuthentication() {
        assertThrows(EmptyResultDataAccessException.class,
                () -> utenteDao.findByEmail("admin@test.it'--"),
                "Comment injection must not truncate the WHERE clause or allow authentication bypass");
    }

    @Test
    @DisplayName("SQL Injection - stacked query in findByEmail must not execute DDL (WSTG-INPV-05)")
    void testFindByEmail_WithStackedQueryInjection_ShouldNotExecuteDDL() {
        assertThrows(EmptyResultDataAccessException.class,
                () -> utenteDao.findByEmail("x'; DROP TABLE utenti; --"),
                "Stacked query injection must be treated as a literal string and must not execute DDL statements");
    }

    @Test
    @DisplayName("SQL Injection - UNION injection in findEmailByUid must not leak email data (WSTG-INPV-05)")
    void testFindEmailByUid_WithUnionInjection_ShouldNotLeakData() {
        assertThrows(EmptyResultDataAccessException.class,
                () -> utenteDao.findEmailByUid("x' UNION SELECT email FROM utenti LIMIT 1 --"),
                "UNION injection in uid must be neutralized by parameterized query");
    }

    // ============================================================================
    // OWASP WSTG-AUTHN-03: Testing for Weak Lock Out Mechanism
    // Verifica che i dati di protezione brute-force (failedLoginAttempts e lockedUntil)
    // vengano persistiti correttamente, garantendo l'integrità del meccanismo di lockout.
    // ============================================================================

    @Test
    @DisplayName("Brute Force Protection - updateLoginAttempts must persist lockout data correctly (WSTG-AUTHN-03)")
    void testUpdateLoginAttempts_ShouldPersistBruteForceProtectionData() {
        java.time.Instant lockUntil = java.time.Instant.now().plusSeconds(900);
        Utente utente = utenteDao.findByEmail("cliente1@test.it");
        utente.setFailedLoginAttempts(5);
        utente.setLockedUntil(lockUntil);

        utenteDao.updateLoginAttempts(utente);

        Integer storedAttempts = jdbcTemplate.queryForObject(
                "SELECT failedloginattempts FROM utenti WHERE uid = ?", Integer.class, utente.getUid());
        java.sql.Timestamp storedLock = jdbcTemplate.queryForObject(
                "SELECT lockeduntil FROM utenti WHERE uid = ?", java.sql.Timestamp.class, utente.getUid());

        assertEquals(5, storedAttempts,
                "failedLoginAttempts must be persisted exactly — a mismatch would break the lockout threshold");
        assertNotNull(storedLock,
                "lockedUntil must be persisted — a null value would allow indefinite login attempts");
        assertTrue(storedLock.toInstant().isAfter(java.time.Instant.now()),
                "lockedUntil must be a future timestamp to enforce the account lockout");
    }

    // ============================================================================
    // OWASP WSTG-INPV-01: Uniqueness constraint — duplicate email must be rejected
    // Verifica che il DAO non permetta la registrazione di due account con la stessa email,
    // prevenendo account hijacking tramite email già in uso.
    // ============================================================================

    @Test
    @DisplayName("Uniqueness constraint - duplicate email must raise DataIntegrityViolationException (WSTG-INPV-01)")
    void testSave_WithDuplicateEmail_ShouldThrowDataIntegrityViolation() {
        String uid1 = UUID.randomUUID().toString();
        String uid2 = UUID.randomUUID().toString();
        String sharedEmail = uid1 + "@duplicate-test.com";

        utenteDao.save(new Utente(uid1, sharedEmail, "hash1", "Cliente"));

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> utenteDao.save(new Utente(uid2, sharedEmail, "hash2", "Cliente")),
                "DAO must enforce the UNIQUE constraint on email — two accounts with the same email would allow account takeover");
    }
}
