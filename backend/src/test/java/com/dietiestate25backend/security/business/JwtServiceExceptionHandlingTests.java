package com.dietiestate25backend.security.business;

import com.dietiestate25backend.TestConfiguration;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * JwtService Exception Handling Tests
 *
 * Business Logic Security - Phase 4
 *
 * Verifica che il service non leaki informazioni sul token (scaduto, malformato, ecc.)
 * Tutte le eccezioni di validazione devono ritornare lo stesso messaggio generico.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("JwtService Exception Handling Tests - Business Logic Security")
class JwtServiceExceptionHandlingTests {

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    /**
     * Token malformato durante validazione.
     *
     * Precondizione: jwtDecoder.decode() lancia Exception per token malformato
     * Azione: validateAndDecodeToken() è chiamato
     * Aspettativa: UnauthorizedException lanciato con messaggio generico
     */
    @Test
    @DisplayName("Malformed token - SHOULD throw UnauthorizedException with generic message")
    void testValidateAndDecodeToken_MalformedToken_ShouldThrowUnauthorized() {
        String malformedToken = "invalid.token.format";

        when(jwtDecoder.decode(malformedToken))
                .thenThrow(new RuntimeException("Invalid token"));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> jwtService.validateAndDecodeToken(malformedToken),
                "Malformed token dovrebbe lanciare UnauthorizedException"
        );

        assert exception.getMessage().equals("Token non valido o scaduto");
    }

    /**
     * Token scaduto durante validazione.
     *
     * Precondizione: jwtDecoder.decode() lancia Exception per token scaduto
     * Azione: validateAndDecodeToken() è chiamato
     * Aspettativa: UnauthorizedException lanciato con stesso messaggio generico
     */
    @Test
    @DisplayName("Expired token - SHOULD throw UnauthorizedException with generic message")
    void testValidateAndDecodeToken_ExpiredToken_ShouldThrowUnauthorized() {
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired.signature";

        when(jwtDecoder.decode(expiredToken))
                .thenThrow(new RuntimeException("Token expired"));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> jwtService.validateAndDecodeToken(expiredToken),
                "Expired token dovrebbe lanciare UnauthorizedException"
        );

        assert exception.getMessage().equals("Token non valido o scaduto");
    }

    /**
     * Token con firma non valida durante validazione.
     *
     * Precondizione: jwtDecoder.decode() lancia Exception per firma non valida
     * Azione: validateAndDecodeToken() è chiamato
     * Aspettativa: UnauthorizedException lanciato con stesso messaggio generico
     */
    @Test
    @DisplayName("Invalid signature - SHOULD throw UnauthorizedException with generic message")
    void testValidateAndDecodeToken_InvalidSignature_ShouldThrowUnauthorized() {
        String invalidSignatureToken = "valid.token.butinvalidsignature";

        when(jwtDecoder.decode(invalidSignatureToken))
                .thenThrow(new RuntimeException("Invalid signature"));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> jwtService.validateAndDecodeToken(invalidSignatureToken),
                "Invalid signature dovrebbe lanciare UnauthorizedException"
        );

        assert exception.getMessage().equals("Token non valido o scaduto");
    }

    /**
     * extractSubject su token non valido.
     *
     * Precondizione: validateAndDecodeToken() lancia UnauthorizedException
     * Azione: extractSubject() è chiamato
     * Aspettativa: UnauthorizedException propagato
     */
    @Test
    @DisplayName("Extract subject from invalid token - SHOULD propagate UnauthorizedException")
    void testExtractSubject_InvalidToken_ShouldThrowUnauthorized() {
        String invalidToken = "invalid.token";

        when(jwtDecoder.decode(invalidToken))
                .thenThrow(new RuntimeException("Invalid token"));

        assertThrows(
                UnauthorizedException.class,
                () -> jwtService.extractSubject(invalidToken),
                "Invalid token dovrebbe lanciare UnauthorizedException"
        );
    }

    /**
     * extractRole su token non valido.
     *
     * Precondizione: validateAndDecodeToken() lancia UnauthorizedException
     * Azione: extractRole() è chiamato
     * Aspettativa: UnauthorizedException propagato
     */
    @Test
    @DisplayName("Extract role from invalid token - SHOULD propagate UnauthorizedException")
    void testExtractRole_InvalidToken_ShouldThrowUnauthorized() {
        String invalidToken = "invalid.token";

        when(jwtDecoder.decode(invalidToken))
                .thenThrow(new RuntimeException("Invalid token"));

        assertThrows(
                UnauthorizedException.class,
                () -> jwtService.extractRole(invalidToken),
                "Invalid token dovrebbe lanciare UnauthorizedException"
        );
    }
}
