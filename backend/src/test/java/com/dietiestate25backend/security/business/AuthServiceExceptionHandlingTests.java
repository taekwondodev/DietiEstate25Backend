package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.dto.requests.LoginRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.error.exception.ConflictException;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.model.Utente;
import com.dietiestate25backend.service.AuthService;
import com.dietiestate25backend.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * AuthService Exception Handling Tests - Business Logic Security
 *
 * Verifica che il service non leaki informazioni sul fatto che un email esista
 * durante login o registrazione. Tutte le eccezioni devono essere generiche.
 */
@DisplayName("AuthService Exception Handling Tests - Business Logic Security")
class AuthServiceExceptionHandlingTests extends BaseIntegrationTest {

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

    @Test
    @DisplayName("Login with non-existent email - SHOULD throw UnauthorizedException")
    void testLogin_EmailNotFound_ShouldThrowUnauthorized() {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

        when(utenteDao.findByEmail("nonexistent@example.com"))
                .thenThrow(new org.springframework.dao.EmptyResultDataAccessException(1));

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );
    }

    @Test
    @DisplayName("Login with incorrect password - SHOULD throw UnauthorizedException")
    void testLogin_IncorrectPassword_ShouldThrowUnauthorized() {
        LoginRequest request = new LoginRequest("user@example.com", "wrongpassword");

        Utente utente = new Utente("uid123", "user@example.com", "hashedPassword", "Cliente");
        when(utenteDao.findByEmail("user@example.com")).thenReturn(utente);
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );
    }

    @Test
    @DisplayName("Register with existing email - SHOULD throw ConflictException")
    void testRegistraCliente_EmailAlreadyExists_ShouldThrowConflict() {
        RegistrazioneRequest request = new RegistrazioneRequest("existing@example.com", "password123", "Cliente");

        when(utenteDao.save(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        assertThrows(
                ConflictException.class,
                () -> authService.registraCliente(request)
        );
    }

    @Test
    @DisplayName("Register - DAO throws DataAccessException - SHOULD wrap without leaking")
    void testRegistraCliente_DAOThrowsDataAccessException_ShouldWrapGeneric() {
        RegistrazioneRequest request = new RegistrazioneRequest("new@example.com", "password123", "Cliente");

        when(utenteDao.save(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DataAccessException("Database unavailable") {});

        assertThrows(
                InternalServerErrorException.class,
                () -> authService.registraCliente(request)
        );
    }

    @Test
    @DisplayName("Login - DAO throws DataAccessException - SHOULD wrap appropriately")
    void testLogin_DAOThrowsDataAccessException_ShouldWrapAsNotFound() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        when(utenteDao.findByEmail("user@example.com"))
                .thenThrow(new DataAccessException("Connection lost") {});

        assertThrows(
                NotFoundException.class,
                () -> authService.login(request)
        );
    }
}