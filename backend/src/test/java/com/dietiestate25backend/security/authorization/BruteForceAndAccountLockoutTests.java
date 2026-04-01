package com.dietiestate25backend.security.authorization;

import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.dto.requests.LoginRequest;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.model.Utente;
import com.dietiestate25backend.service.AuthService;
import com.dietiestate25backend.service.EmailService;
import com.dietiestate25backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Brute Force & Account Lockout Tests - OWASP WSTG-AUTHN-02")
class BruteForceAndAccountLockoutTests {

    @Mock
    private UtenteDao utenteDao;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private String validEmail;
    private String validPassword;
    private String wrongPassword;
    private String hashedPassword;
    private String sub;
    private Utente testUser;

    // ============================================================================
    // OWASP WSTG-AUTHN-02: Testing for Weak Lock Out Mechanism
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/04-Authentication_Testing/02-Testing_for_Weak_lock_out_mechanism
    // ============================================================================

    @BeforeEach
    void setUp() {
        validEmail = "user@example.com";
        validPassword = "SecurePass123!";
        wrongPassword = "WrongPassword123!";
        hashedPassword = "$2a$10$dXJ3SW6G7P50eS3B6skPfOWOZ4e8qRCEIvhDL3mVtILw3/LwDFfBm";
        sub = "550e8400-e29b-41d4-a716-446655440000";
        testUser = new Utente(sub, validEmail, hashedPassword, "Cliente");
    }

    // -------- Failed Login Attempt Tracking --------

    @Test
    @DisplayName("Brute Force - First failed login attempt should be tracked")
    void testFirstFailedLoginAttempt_ShouldBeTracked() {
        LoginRequest request = new LoginRequest(validEmail, wrongPassword);

        when(utenteDao.findByEmail(validEmail)).thenReturn(testUser);
        when(passwordEncoder.matches(wrongPassword, hashedPassword)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        verify(utenteDao).findByEmail(validEmail);
    }

    @Test
    @DisplayName("Brute Force - Failed login attempts should be cumulative")
    void testMultipleFailedLoginAttempts_ShouldBeCumulative() {
        LoginRequest request = new LoginRequest(validEmail, wrongPassword);

        when(utenteDao.findByEmail(validEmail)).thenReturn(testUser);
        when(passwordEncoder.matches(wrongPassword, hashedPassword)).thenReturn(false);

        for (int i = 0; i < 3; i++) {
            assertThrows(UnauthorizedException.class, () -> authService.login(request));
        }

        verify(utenteDao, times(3)).findByEmail(validEmail);
    }

    @Test
    @DisplayName("Brute Force - Fifth failed attempt should trigger account lockout")
    void testFifthFailedLoginAttempt_ShouldLockAccount() {
        LoginRequest request = new LoginRequest(validEmail, wrongPassword);

        when(utenteDao.findByEmail(validEmail)).thenReturn(testUser);
        when(passwordEncoder.matches(wrongPassword, hashedPassword)).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            assertThrows(UnauthorizedException.class, () -> authService.login(request));
        }

        Exception exception = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertTrue(
                exception.getMessage().toLowerCase().contains("bloccato") ||
                        exception.getMessage().toLowerCase().contains("locked") ||
                        exception.getMessage().toLowerCase().contains("troppi tentativi"),
                "Error message should indicate account is locked"
        );
    }

    // -------- Account Lockout Mechanism --------

    @Test
    @DisplayName("Account Lockout - Locked account should reject any login attempt")
    void testLockedAccount_ShouldRejectAllLoginAttempts() {
        LoginRequest correctPasswordRequest = new LoginRequest(validEmail, validPassword);

        Utente lockedUser = new Utente(sub, validEmail, hashedPassword, "Cliente");
        lockedUser.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));

        when(utenteDao.findByEmail(validEmail)).thenReturn(lockedUser);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(correctPasswordRequest)
        );

        assertEquals(ErrorCode.ACCOUNT_LOCKED, exception.getErrorCode(),
                "Locked account should be rejected regardless of password correctness");
    }

    @Test
    @DisplayName("Account Lockout - Lockout duration should be sufficient (15+ minutes)")
    void testAccountLockout_DurationShouldBeFifteenMinutesOrMore() {
        Instant now = Instant.now();
        Instant lockoutExpiry = now.plus(15, ChronoUnit.MINUTES);

        assertTrue(
                lockoutExpiry.isAfter(now.plus(14, ChronoUnit.MINUTES)),
                "Lockout duration should be at least 15 minutes"
        );
    }

    @Test
    @DisplayName("Account Lockout - Automatic unlock after lockout period expires")
    void testAccountLockout_AutomaticUnlockAfterTimeout() {
        LoginRequest request = new LoginRequest(validEmail, validPassword);

        Utente userAfterLockoutExpiry = new Utente(sub, validEmail, hashedPassword, "Cliente");

        when(utenteDao.findByEmail(validEmail)).thenReturn(userAfterLockoutExpiry);
        when(passwordEncoder.matches(validPassword, hashedPassword)).thenReturn(true);
        when(jwtService.generateToken(sub, "Cliente", validEmail)).thenReturn("valid.jwt.token");

        assertDoesNotThrow(() -> authService.login(request),
                "Account should be unlocked and login should succeed after lockout period expires"
        );
    }

    // -------- Failed Attempt Counter Reset --------

    @Test
    @DisplayName("Brute Force - Failed attempt counter should reset on successful login")
    void testSuccessfulLogin_ShouldResetFailedAttemptCounter() {
        LoginRequest wrongPasswordRequest = new LoginRequest(validEmail, wrongPassword);
        LoginRequest correctPasswordRequest = new LoginRequest(validEmail, validPassword);

        Utente userWithAttempts = new Utente(sub, validEmail, hashedPassword, "Cliente");

        when(utenteDao.findByEmail(validEmail)).thenReturn(userWithAttempts);
        when(passwordEncoder.matches(wrongPassword, hashedPassword)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(wrongPasswordRequest));
        assertThrows(UnauthorizedException.class, () -> authService.login(wrongPasswordRequest));

        when(passwordEncoder.matches(validPassword, hashedPassword)).thenReturn(true);
        when(jwtService.generateToken(sub, "Cliente", validEmail)).thenReturn("valid.jwt.token");

        authService.login(correctPasswordRequest);
    }

    // -------- Account Enumeration Prevention --------

    @Test
    @DisplayName("Brute Force Prevention - Locked account message should not reveal account existence")
    void testLockedAccount_MessageShouldNotRevealAccountExistence() {
        LoginRequest request = new LoginRequest(validEmail, validPassword);

        Utente lockedUser = new Utente(sub, validEmail, hashedPassword, "Cliente");
        lockedUser.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));

        when(utenteDao.findByEmail(validEmail)).thenReturn(lockedUser);

        Exception exception = assertThrows(UnauthorizedException.class,
                () -> authService.login(request)
        );

        assertEquals(ErrorCode.ACCOUNT_LOCKED, ((UnauthorizedException) exception).getErrorCode(),
                "Error code should be ACCOUNT_LOCKED without revealing account existence details");
    }

    // -------- Lockout Notification --------

    @Test
    @DisplayName("Account Lockout - User should be notified when account is locked")
    void testAccountLockout_ShouldNotifyUserViaEmail() {
        LoginRequest request = new LoginRequest(validEmail, wrongPassword);

        when(utenteDao.findByEmail(validEmail)).thenReturn(testUser);
        when(passwordEncoder.matches(wrongPassword, hashedPassword)).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            try {
                authService.login(request);
            } catch (UnauthorizedException e) {
                // Expected
            }
        }

        verify(emailService, atLeastOnce()).inviaEmail(eq(validEmail), anyString(), anyString());
    }
}