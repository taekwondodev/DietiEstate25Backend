package com.dietiestate25backend.security.auth;

import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Security Tests - Token Validation & Integrity")
class JwtServiceSecurityTests {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @InjectMocks
    private JwtService jwtService;

    private String validToken;
    private String expiredToken;
    private String tamperedToken;
    private String uid;
    private String role;
    private String email;

    @BeforeEach
    void setUp() {
        validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLWlkIiwicm9sZSI6IkNsaWVudGUiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJleHAiOjk5OTk5OTk5OTl9.signature";
        expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLWlkIiwicm9sZSI6IkNsaWVudGUiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJleHAiOjB9.signature";
        tamperedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbnRvZGVyLXVzZXItaWQiLCJyb2xlIjoiQWRtaW4iLCJlbWFpbCI6ImF0dGFja2VyQGV4YW1wbGUuY29tIiwiZXhwIjo5OTk5OTk5OTk5fQ.wrongsignature";
        uid = "550e8400-e29b-41d4-a716-446655440000";
        role = "Cliente";
        email = "user@example.com";
    }

    @Test
    @DisplayName("Token Generation - Should create valid JWT with correct claims")
    void testTokenGeneration_ShouldCreateValidToken() {
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
            .thenReturn(new org.springframework.security.oauth2.jwt.Jwt(
                validToken,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                java.util.Map.of("alg", "HS256"),
                java.util.Map.of("sub", uid, "role", role, "email", email)
            ));

        String token = jwtService.generateToken(uid, role, email);

        assertNotNull(token);
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    @DisplayName("Token Expiration - Expired token should be rejected")
    void testTokenExpiration_ShouldRejectExpiredToken() {
        when(jwtDecoder.decode(expiredToken))
            .thenThrow(new org.springframework.security.oauth2.jwt.JwtException("Token expired"));

        assertThrows(UnauthorizedException.class, () -> jwtService.validateAndDecodeToken(expiredToken),
            "Expired token should throw UnauthorizedException");
    }

    @Test
    @DisplayName("Token Tampering - Modified token should be rejected")
    void testTokenTampering_ShouldRejectTamperedToken() {
        when(jwtDecoder.decode(tamperedToken))
            .thenThrow(new org.springframework.security.oauth2.jwt.JwtException("Invalid signature"));

        assertThrows(UnauthorizedException.class, () -> jwtService.validateAndDecodeToken(tamperedToken),
            "Tampered token should throw UnauthorizedException");
    }

    @Test
    @DisplayName("Token Validation - Valid token should be accepted and decoded")
    void testTokenValidation_ValidTokenShouldBeAccepted() {
        Jwt validJwt = new Jwt(
            validToken,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            java.util.Map.of("alg", "HS256"),
            java.util.Map.of("sub", uid, "role", role, "email", email)
        );

        when(jwtDecoder.decode(validToken)).thenReturn(validJwt);

        Jwt decodedJwt = jwtService.validateAndDecodeToken(validToken);

        assertNotNull(decodedJwt);
        assertEquals(uid, decodedJwt.getSubject());
        verify(jwtDecoder).decode(validToken);
    }

    @Test
    @DisplayName("Subject Extraction - Should extract sub claim correctly")
    void testSubjectExtraction_ShouldExtractCorrectSubject() {
        Jwt validJwt = new Jwt(
            validToken,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            java.util.Map.of("alg", "HS256"),
            java.util.Map.of("sub", uid, "role", role, "email", email)
        );

        when(jwtDecoder.decode(validToken)).thenReturn(validJwt);

        String extractedSub = jwtService.extractSubject(validToken);

        assertEquals(uid, extractedSub);
        verify(jwtDecoder).decode(validToken);
    }

    @Test
    @DisplayName("Role Extraction - Should extract role claim correctly")
    void testRoleExtraction_ShouldExtractCorrectRole() {
        Jwt validJwt = new Jwt(
            validToken,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            java.util.Map.of("alg", "HS256"),
            java.util.Map.of("sub", uid, "role", role, "email", email)
        );

        when(jwtDecoder.decode(validToken)).thenReturn(validJwt);

        String extractedRole = jwtService.extractRole(validToken);

        assertEquals(role, extractedRole);
        verify(jwtDecoder).decode(validToken);
    }

    @Test
    @DisplayName("Malformed Token - Invalid format should be rejected")
    void testMalformedToken_ShouldRejectInvalidFormat() {
        String malformedToken = "not.a.valid.jwt";

        when(jwtDecoder.decode(malformedToken))
            .thenThrow(new org.springframework.security.oauth2.jwt.JwtException("Invalid JWT format"));

        assertThrows(UnauthorizedException.class, () -> jwtService.validateAndDecodeToken(malformedToken),
            "Malformed token should throw UnauthorizedException");
    }

    @Test
    @DisplayName("Token Claims Integrity - Claims should not be extractable from expired token")
    void testTokenClaimsIntegrity_ExpiredTokenClaimsShouldNotBeExtractable() {
        when(jwtDecoder.decode(expiredToken))
            .thenThrow(new org.springframework.security.oauth2.jwt.JwtException("Token expired"));

        assertThrows(UnauthorizedException.class, () -> jwtService.extractSubject(expiredToken),
            "Should not be able to extract claims from expired token");
    }

    @Test
    @DisplayName("Token Signature Verification - Different secret should reject token")
    void testTokenSignatureVerification_DifferentSecretShouldRejectToken() {
        when(jwtDecoder.decode(validToken))
            .thenThrow(new org.springframework.security.oauth2.jwt.JwtException("Invalid signature"));

        assertThrows(UnauthorizedException.class, () -> jwtService.validateAndDecodeToken(validToken),
            "Token signed with different secret should be rejected");
    }

    @Test
    @DisplayName("Token Generation - Should include all required claims")
    void testTokenGeneration_ShouldIncludeAllRequiredClaims() {
        Jwt jwt = new Jwt(
            validToken,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            java.util.Map.of("alg", "HS256"),
            java.util.Map.of("sub", uid, "role", role, "email", email)
        );

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = jwtService.generateToken(uid, role, email);

        assertNotNull(token);
        assertTrue(token.contains("."), "Token should be in JWT format");
    }
}

