package com.dietiestate25backend.service;

import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {
    private static final long JWTEXPIRATION = 3600000; // 1 ora in millisecondi

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Genera un JWT con sub e role
     */
    public String generateToken(String sub, String role, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(JWTEXPIRATION);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(sub)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("role", role)
                .claim("email", email)
                .build();

        return jwtEncoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    /**
     * Valida il token e ritorna il JWT decodificato
     */
    public Jwt validateAndDecodeToken(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * Estrae il subject dal token
     */
    public String extractSubject(String token) {
        return validateAndDecodeToken(token).getSubject();
    }

    /**
     * Estrae il role dal token
     */
    public String extractRole(String token) {
        Jwt jwt = validateAndDecodeToken(token);
        return jwt.getClaimAsString("role");
    }
}