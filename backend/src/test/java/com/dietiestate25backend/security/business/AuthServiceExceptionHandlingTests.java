package com.dietiestate25backend.security.business;

import com.dietiestate25backend.TestConfiguration;
import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.dto.requests.LoginRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.error.exception.ConflictException;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.model.Utente;
import com.dietiestate25backend.service.AuthService;
import com.dietiestate25backend.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * AuthService Exception Handling Tests
 *
 * Business Logic Security - Phase 4
 *
 * Verifica che il service non leaki informazioni sul fatto che un email esista
 * durante login o registrazione. Tutte le eccezioni devono essere generiche.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("AuthService Exception Handling Tests - Business Logic Security")
class AuthServiceExceptionHandlingTests {

    @Autowired
    private AuthService authService;

    @MockitoBean
    private UtenteDao utenteDao;

    @MockitoBean
    private UtenteAgenziaDao utenteAgenziaDao;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    /**
     * Email non trovata durante login.
     *
     * Precondizione: utenteDao.findByEmail() lancia EmptyResultDataAccessException
     * Azione: login() è chiamato con email inesistente
     * Aspettativa: NotFoundException lanciato con messaggio generico
     */
    @Test
    @DisplayName("Login with non-existent email - SHOULD throw NotFoundException")
    void testLogin_EmailNotFound_ShouldThrowNotFound() {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

        when(utenteDao.findByEmail("nonexistent@example.com"))
                .thenThrow(new org.springframework.dao.EmptyResultDataAccessException(1));

        assertThrows(
                NotFoundException.class,
                () -> authService.login(request),
                "Non-existent email dovrebbe lanciare NotFoundException"
        );
    }

    /**
     * Password non corretta durante login.
     *
     * Precondizione: Utente trovato ma password non corretta
     * Azione: login() è chiamato con password sbagliata
     * Aspettativa: NotFoundException lanciato (non differenziare tra email e password)
     */
    @Test
    @DisplayName("Login with incorrect password - SHOULD throw NotFoundException")
    void testLogin_IncorrectPassword_ShouldThrowNotFound() {
        LoginRequest request = new LoginRequest("user@example.com", "wrongpassword");

        Utente utente = new Utente("uid123", "user@example.com", "hashedPassword", "Cliente");
        when(utenteDao.findByEmail("user@example.com")).thenReturn(utente);
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        assertThrows(
                NotFoundException.class,
                () -> authService.login(request),
                "Incorrect password dovrebbe lanciare NotFoundException"
        );
    }

    /**
     * Email già registrata durante registrazione.
     *
     * Precondizione: utenteDao.save() lancia DataIntegrityViolationException
     * Azione: registraCliente() è chiamato con email già usata
     * Aspettativa: ConflictException lanciato
     */
    @Test
    @DisplayName("Register with existing email - SHOULD throw ConflictException")
    void testRegistraCliente_EmailAlreadyExists_ShouldThrowConflict() {
        RegistrazioneRequest request = new RegistrazioneRequest("existing@example.com", "password123", "Cliente");

        when(utenteDao.save(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        assertThrows(
                ConflictException.class,
                () -> authService.registraCliente(request),
                "Duplicate email dovrebbe lanciare ConflictException"
        );
    }

    /**
     * DAO lancia DataAccessException durante registrazione.
     *
     * Precondizione: utenteDao.save() lancia DataAccessException
     * Azione: registraCliente() è chiamato
     * Aspettativa: InternalServerErrorException lanciato senza leakage
     */
    @Test
    @DisplayName("Register - DAO throws DataAccessException - SHOULD wrap without leaking")
    void testRegistraCliente_DAOThrowsDataAccessException_ShouldWrapGeneric() {
        RegistrazioneRequest request = new RegistrazioneRequest("new@example.com", "password123", "Cliente");

        when(utenteDao.save(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DataAccessException("Database unavailable") {});

        assertThrows(
                InternalServerErrorException.class,
                () -> authService.registraCliente(request),
                "DataAccessException dovrebbe essere wrapped generically"
        );
    }

    /**
     * DAO lancia DataAccessException durante login.
     *
     * Precondizione: utenteDao.findByEmail() lancia DataAccessException
     * Azione: login() è chiamato
     * Aspettativa: NotFoundException lanciato (mantiene coerenza con EmptyResultDataAccessException)
     */
    @Test
    @DisplayName("Login - DAO throws DataAccessException - SHOULD wrap appropriately")
    void testLogin_DAOThrowsDataAccessException_ShouldWrapAsNotFound() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        when(utenteDao.findByEmail("user@example.com"))
                .thenThrow(new DataAccessException("Connection lost") {});

        assertThrows(
                NotFoundException.class,
                () -> authService.login(request),
                "Database error during login dovrebbe lanciare NotFoundException per coerenza"
        );
    }
}
