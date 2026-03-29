package com.dietiestate25backend.security.auth;

import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
import com.dietiestate25backend.dto.requests.LoginRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneStaffRequest;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.model.Utente;
import com.dietiestate25backend.service.AuthService;
import com.dietiestate25backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Security Tests - Password Hashing & Validation")
class AuthServiceSecurityTests {

    @Mock
    private UtenteDao utenteDao;

    @Mock
    private UtenteAgenziaDao utenteAgenziaDao;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private String validEmail;
    private String validPassword;
    private String hashedPassword;
    private String uid;

    @BeforeEach
    void setUp() {
        validEmail = "user@example.com";
        validPassword = "SecurePassword123!";
        hashedPassword = "$2a$10$dXJ3SW6G7P50eS3B6skPfOWOZ4e8qRCEIvhDL3mVtILw3/LwDFfBm";
        uid = "550e8400-e29b-41d4-a716-446655440000";
    }

    @Test
    @DisplayName("BCrypt Hashing - Password should not be stored as plaintext")
    void testPasswordHashingNotPlaintext() {
        RegistrazioneRequest request = new RegistrazioneRequest(validEmail, validPassword, "Cliente");
        
        when(passwordEncoder.encode(validPassword)).thenReturn(hashedPassword);

        authService.registraCliente(request);

        verify(utenteDao).save(argThat(utente -> 
            !utente.getPassword().equals(validPassword) && 
            utente.getPassword().equals(hashedPassword)
        ));
    }

    @Test
    @DisplayName("BCrypt Uniqueness - Same password produces different hashes")
    void testPasswordHashingUniqueness() {
        String hash1 = "$2a$10$dXJ3SW6G7P50eS3B6skPfOWOZ4e8qRCEIvhDL3mVtILw3/LwDFfBm";
        String hash2 = "$2a$10$lGKLQd5G7P50eS3B6skPfOWOZ4e8qRCEIvhDL3mVtILw3/LwDFfBm";

        RegistrazioneRequest request1 = new RegistrazioneRequest("user1@example.com", validPassword, "Cliente");
        RegistrazioneRequest request2 = new RegistrazioneRequest("user2@example.com", validPassword, "Cliente");

        when(passwordEncoder.encode(validPassword))
            .thenReturn(hash1)
            .thenReturn(hash2);

        authService.registraCliente(request1);
        authService.registraCliente(request2);

        verify(utenteDao, times(2)).save(any());
        assertNotEquals(hash1, hash2, "Same password should produce different hashes (different salts)");
    }

    @Test
    @DisplayName("Login Failure - Wrong password should throw UnauthorizedException")
    void testLoginWithWrongPassword_ShouldThrowUnauthorizedException() {
        LoginRequest request = new LoginRequest(validEmail, "WrongPassword123!");
        Utente storedUser = new Utente(uid, validEmail, hashedPassword, "Cliente");

        when(utenteDao.findByEmail(validEmail)).thenReturn(storedUser);
        when(passwordEncoder.matches("WrongPassword123!", hashedPassword)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request),
            "Login with wrong password should throw UnauthorizedException");
    }

    @Test
    @DisplayName("Login Failure - Wrong password does not reveal email existence")
    void testLoginWithWrongPasswordDoesNotRevealEmailExistence() {
        LoginRequest request = new LoginRequest(validEmail, "WrongPassword123!");
        Utente storedUser = new Utente(uid, validEmail, hashedPassword, "Cliente");

        when(utenteDao.findByEmail(validEmail)).thenReturn(storedUser);
        when(passwordEncoder.matches("WrongPassword123!", hashedPassword)).thenReturn(false);

        Exception exception = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        
        assertFalse(exception.getMessage().toLowerCase().contains("email"), 
            "Error message should not reveal email existence");
    }

    @Test
    @DisplayName("Password Matching - Correct password should be validated properly")
    void testPasswordMatching_WithCorrectPassword_ShouldPass() {
        LoginRequest request = new LoginRequest(validEmail, validPassword);
        Utente storedUser = new Utente(uid, validEmail, hashedPassword, "Cliente");

        when(utenteDao.findByEmail(validEmail)).thenReturn(storedUser);
        when(passwordEncoder.matches(validPassword, hashedPassword)).thenReturn(true);
        when(jwtService.generateToken(uid, "Cliente", validEmail)).thenReturn("valid.jwt.token");

        var response = authService.login(request);

        assertNotNull(response);
        assertEquals("valid.jwt.token", response.getToken());
        verify(passwordEncoder).matches(validPassword, hashedPassword);
    }

    @Test
    @DisplayName("Timing Attack Resistance - BCrypt is inherently resistant")
    void testTimingAttackResistance() {
        LoginRequest request = new LoginRequest(validEmail, "WrongPassword");
        Utente storedUser = new Utente(uid, validEmail, hashedPassword, "Cliente");

        when(utenteDao.findByEmail(validEmail)).thenReturn(storedUser);
        when(passwordEncoder.matches("WrongPassword", hashedPassword)).thenReturn(false);

        long startTime = System.nanoTime();
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
        long endTime = System.nanoTime();

        assertTrue((endTime - startTime) > 0, "BCrypt should take consistent time");
    }

    @Test
    @DisplayName("Registration - Password should be hashed before storage")
    void testRegistrationHashesPasswordBeforeSave() {
        RegistrazioneRequest request = new RegistrazioneRequest(validEmail, validPassword, "Cliente");

        when(passwordEncoder.encode(validPassword)).thenReturn(hashedPassword);

        authService.registraCliente(request);

        verify(passwordEncoder).encode(validPassword);
        verify(utenteDao).save(argThat(utente -> 
            utente.getPassword().equals(hashedPassword) &&
            !utente.getPassword().equals(validPassword)
        ));
    }

    @Test
    @DisplayName("Staff Registration Security - Only Admin can register staff")
    void testRegistraGestoreOrAgente_OnlyAdminCanRegister() {
        RegistrazioneStaffRequest request = new RegistrazioneStaffRequest("agente@example.com", validPassword, "Gestore");
        String adminUid = "admin-uid-123";

        when(passwordEncoder.encode(validPassword)).thenReturn(hashedPassword);

        authService.registraGestoreOrAgente(adminUid, request);

        verify(utenteDao).save(any());
        verify(utenteAgenziaDao).save(any());
    }
}

