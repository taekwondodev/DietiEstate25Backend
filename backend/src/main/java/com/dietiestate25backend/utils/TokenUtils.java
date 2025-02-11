package com.dietiestate25backend.utils;

import com.dietiestate25backend.error.exception.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Optional;

public class TokenUtils {

    private TokenUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Ottiene il token JWT dell'utente autenticato
     */
    private static Optional<Jwt> getJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(jwtAuth.getToken());
        }
        return Optional.empty();
    }

    /**
     * Ottiene il valore del claim "sub" (UUID dell'utente Cognito)
     */
    public static String getUserSub() {
        Optional<String> sub = getJwt().map(jwt -> jwt.getClaim("sub"));

        if (sub.isEmpty()) {
            throw new UnauthorizedException("Utente non trovato");
        } else {
            return sub.get();
        }
    }

    public static void checkIfAdmin() {
        Optional<String> role = getJwt().map(jwt -> jwt.getClaim("custom:role"));

        if (role.isEmpty() || !role.get().equals("Admin")) {
            throw new UnauthorizedException("Non hai i permessi per eseguire questa operazione");
        }
    }
}
