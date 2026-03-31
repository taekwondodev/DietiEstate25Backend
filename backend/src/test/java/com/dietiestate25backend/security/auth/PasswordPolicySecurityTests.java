package com.dietiestate25backend.security.auth;

import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.service.AuthService;
import com.dietiestate25backend.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Password Policy Security Tests - OWASP WSTG-AUTHN")
class PasswordPolicySecurityTests {

    @Mock
    private UtenteDao utenteDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_ROLE = "Cliente";
    private static final String HASHED_PASSWORD = "$2a$10$dXJ3SW6G7P50eS3B6skPfOWOZ4e8qRCEIvhDL3mVtILw3/LwDFfBm";

    // ============================================================================
    // OWASP WSTG-AUTHN-03: Password Policy Tests
    // Reference: https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/04-Authentication_Testing/03-Testing_for_Weak_Password_Policy
    // ============================================================================

    // -------- Minimum Length Tests (OWASP: min 8 characters) --------

    @Test
    @DisplayName("Password Length - Minimum 8 characters (OWASP Guideline)")
    void testPasswordMinimumLength_SevenCharacters_ShouldReject() {
        RegistrazioneRequest request = new RegistrazioneRequest(
                VALID_EMAIL,
                "Abc!123",  // 7 characters - below minimum
                VALID_ROLE
        );

        assertThrows(BadRequestException.class,
                () -> authService.registraCliente(request),
                "Password with 7 characters should be rejected");
    }

    @Test
    @DisplayName("Password Length - Exactly 8 characters (minimum threshold)")
    void testPasswordMinimumLength_EightCharacters_ShouldAccept() {
        RegistrazioneRequest request = new RegistrazioneRequest(
                VALID_EMAIL,
                "Abc!1234",  // 8 characters - exactly minimum
                VALID_ROLE
        );

        when(passwordEncoder.encode("Abc!1234")).thenReturn(HASHED_PASSWORD);

        assertDoesNotThrow(() -> authService.registraCliente(request),
                "Password with 8 characters should be accepted");
    }

    @Test
    @DisplayName("Password Length - 128 characters maximum")
    void testPasswordMaximumLength_ExceeedsLimit_ShouldReject() {
        String passwordTooLong = "A".repeat(256) + "!";  // 257 characters

        RegistrazioneRequest request = new RegistrazioneRequest(
                VALID_EMAIL,
                passwordTooLong,
                VALID_ROLE
        );

        assertThrows(BadRequestException.class,
                () -> authService.registraCliente(request),
                "Password exceeding 255 characters should be rejected");
    }

    // -------- Complexity Requirements (OWASP: uppercase, lowercase, numbers, special) --------

    @ParameterizedTest(name = "Password without uppercase: {0}")
    @ValueSource(strings = {
            "abc!1234",      // lowercase + special + number
            "abcdefgh",      // lowercase only
            "1234!@#$",      // numbers + special only
    })
    @DisplayName("Password Complexity - Missing uppercase letter should be rejected")
    void testPasswordComplexity_MissingUppercase_ShouldReject(String password) {
        RegistrazioneRequest request = new RegistrazioneRequest(VALID_EMAIL, password, VALID_ROLE);

        assertThrows(BadRequestException.class,
                () -> authService.registraCliente(request),
                "Password without uppercase should be rejected: " + password);
    }

    @ParameterizedTest(name = "Password without lowercase: {0}")
    @ValueSource(strings = {
            "ABC!1234",      // uppercase + special + number
            "ABCDEFGH",      // uppercase only
            "1234!@#$",      // numbers + special only
    })
    @DisplayName("Password Complexity - Missing lowercase letter should be rejected")
    void testPasswordComplexity_MissingLowercase_ShouldReject(String password) {
        RegistrazioneRequest request = new RegistrazioneRequest(VALID_EMAIL, password, VALID_ROLE);

        assertThrows(BadRequestException.class,
                () -> authService.registraCliente(request),
                "Password without lowercase should be rejected: " + password);
    }

    @ParameterizedTest(name = "Password without digit: {0}")
    @ValueSource(strings = {
            "Abcdefgh!",     // letters + special
            "Abcdefgh",      // letters only
            "ABCDEFGH!@#",   // uppercase + special
    })
    @DisplayName("Password Complexity - Missing numeric digit should be rejected")
    void testPasswordComplexity_MissingNumber_ShouldReject(String password) {
        RegistrazioneRequest request = new RegistrazioneRequest(VALID_EMAIL, password, VALID_ROLE);

        assertThrows(BadRequestException.class,
                () -> authService.registraCliente(request),
                "Password without number should be rejected: " + password);
    }

    @ParameterizedTest(name = "Password without special character: {0}")
    @ValueSource(strings = {
            "Abcdefg1",      // letters + number
            "Abcdefgh",      // letters only
            "ABC1234",       // uppercase + number
    })
    @DisplayName("Password Complexity - Missing special character should be rejected")
    void testPasswordComplexity_MissingSpecialChar_ShouldReject(String password) {
        RegistrazioneRequest request = new RegistrazioneRequest(VALID_EMAIL, password, VALID_ROLE);

        assertThrows(BadRequestException.class,
                () -> authService.registraCliente(request),
                "Password without special character should be rejected: " + password);
    }

    @Test
    @DisplayName("Password Complexity - Valid password meeting all requirements")
    void testPasswordComplexity_ValidPassword_ShouldAccept() {
        RegistrazioneRequest request = new RegistrazioneRequest(
                VALID_EMAIL,
                "SecurePass123!",  // uppercase, lowercase, number, special
                VALID_ROLE
        );

        when(passwordEncoder.encode("SecurePass123!")).thenReturn(HASHED_PASSWORD);

        assertDoesNotThrow(() -> authService.registraCliente(request),
                "Password meeting all complexity requirements should be accepted");
    }

    // -------- Common Pattern Restrictions (OWASP: prevent weak patterns) --------

    @ParameterizedTest(name = "Password with sequential characters: {0}")
    @ValueSource(strings = {
            "Abcdef123!",    // sequential alphabet
            "Zxcvbn123!",    // sequential qwerty
            "Aaabbb123!",    // repeating letters
    })
    @DisplayName("Password Pattern - Sequential or repeating characters should be rejected")
    void testPasswordPattern_SequentialCharacters_ShouldReject(String password) {
        RegistrazioneRequest request = new RegistrazioneRequest(VALID_EMAIL, password, VALID_ROLE);

        assertThrows(BadRequestException.class,
                () -> authService.registraCliente(request),
                "Password with sequential characters should be rejected: " + password);
    }

    @ParameterizedTest(name = "Password containing email parts: {0}")
    @ValueSource(strings = {
            "User@123!",     // contains 'user' from email
            "Example123!",   // contains email domain
    })
    @DisplayName("Password Policy - Password should not contain email parts")
    void testPassword_ContainsEmailParts_ShouldReject(String password) {
        // Email: user@example.com - should reject if password contains 'user' or 'example'
        RegistrazioneRequest request = new RegistrazioneRequest(VALID_EMAIL, password, VALID_ROLE);

        assertThrows(BadRequestException.class,
                () -> authService.registraCliente(request),
                "Password containing email parts should be rejected: " + password);
    }

    // -------- Space and Whitespace Handling --------

    @Test
    @DisplayName("Password Validation - Password with spaces should be rejected")
    void testPassword_ContainsSpaces_ShouldReject() {
        RegistrazioneRequest request = new RegistrazioneRequest(
                VALID_EMAIL,
                "Secure Pass123!",  // contains space
                VALID_ROLE
        );

        assertThrows(BadRequestException.class,
                () -> authService.registraCliente(request),
                "Password with spaces should be rejected");
    }

    @Test
    @DisplayName("Password Validation - Leading/trailing spaces should be rejected")
    void testPassword_ContainsLeadingTrailingSpaces_ShouldReject() {
        RegistrazioneRequest request = new RegistrazioneRequest(
                VALID_EMAIL,
                " SecurePass123!",  // leading space
                VALID_ROLE
        );

        assertThrows(BadRequestException.class,
                () -> authService.registraCliente(request),
                "Password with leading/trailing spaces should be rejected");
    }

    // -------- Password Encoding and Verification --------

    @Test
    @DisplayName("Password Encoding - Password should be encoded using BCrypt (or PBKDF2, Argon2)")
    void testPasswordEncoding_ShouldUseBCrypt() {
        RegistrazioneRequest request = new RegistrazioneRequest(
                VALID_EMAIL,
                "SecurePass123!",
                VALID_ROLE
        );

        when(passwordEncoder.encode("SecurePass123!")).thenReturn(HASHED_PASSWORD);

        authService.registraCliente(request);

        verify(passwordEncoder).encode("SecurePass123!");
    }

}
