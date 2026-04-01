package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * JwtService Exception Handling Tests - Business Logic Security
 *
 * Verifica che il service non leaki informazioni sul token (scaduto, malformato, ecc.)
 * Tutte le eccezioni di validazione devono ritornare lo stesso messaggio generico.
 */
@DisplayName("JwtService Exception Handling Tests - Business Logic Security")
class JwtServiceExceptionHandlingTests extends BaseIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("Malformed token - SHOULD throw UnauthorizedException with generic message")
    void testValidateAndDecodeToken_MalformedToken_ShouldThrowUnauthorized() {
        String malformedToken = "invalid.token.format";

        when(jwtDecoder.decode(malformedToken))
                .thenThrow(new RuntimeException("Invalid token"));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> jwtService.validateAndDecodeToken(malformedToken)
        );

        assert exception.getErrorCode() == ErrorCode.INVALID_TOKEN;
    }

    @Test
    @DisplayName("Expired token - SHOULD throw UnauthorizedException with generic message")
    void testValidateAndDecodeToken_ExpiredToken_ShouldThrowUnauthorized() {
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired.signature";

        when(jwtDecoder.decode(expiredToken))
                .thenThrow(new RuntimeException("Token expired"));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> jwtService.validateAndDecodeToken(expiredToken)
        );

        assert exception.getErrorCode() == ErrorCode.INVALID_TOKEN;
    }

    @Test
    @DisplayName("Invalid signature - SHOULD throw UnauthorizedException with generic message")
    void testValidateAndDecodeToken_InvalidSignature_ShouldThrowUnauthorized() {
        String invalidSignatureToken = "valid.token.butinvalidsignature";

        when(jwtDecoder.decode(invalidSignatureToken))
                .thenThrow(new RuntimeException("Invalid signature"));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> jwtService.validateAndDecodeToken(invalidSignatureToken)
        );

        assert exception.getErrorCode() == ErrorCode.INVALID_TOKEN;
    }

    @Test
    @DisplayName("Extract subject from invalid token - SHOULD propagate UnauthorizedException")
    void testExtractSubject_InvalidToken_ShouldThrowUnauthorized() {
        String invalidToken = "invalid.token";

        when(jwtDecoder.decode(invalidToken))
                .thenThrow(new RuntimeException("Invalid token"));

        assertThrows(
                UnauthorizedException.class,
                () -> jwtService.extractSubject(invalidToken)
        );
    }

    @Test
    @DisplayName("Extract role from invalid token - SHOULD propagate UnauthorizedException")
    void testExtractRole_InvalidToken_ShouldThrowUnauthorized() {
        String invalidToken = "invalid.token";

        when(jwtDecoder.decode(invalidToken))
                .thenThrow(new RuntimeException("Invalid token"));

        assertThrows(
                UnauthorizedException.class,
                () -> jwtService.extractRole(invalidToken)
        );
    }
}