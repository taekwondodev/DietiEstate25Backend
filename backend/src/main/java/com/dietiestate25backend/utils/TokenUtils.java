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
        return getJwt()
                .map(jwt -> jwt.getClaimAsString("sub"))
                .orElseThrow(() -> new UnauthorizedException("Sub dell'Utente non trovato"));
    }

    /**
     * Ottiene l'email dell'utente autenticato dal token JWT
     */
    public static String getEmail() {
        return getJwt()
                .map(jwt -> jwt.getClaimAsString("email"))
                .orElseThrow(() -> new UnauthorizedException("Email dell'Utente non trovata"));
    }

    /**
     * Ottiene il valore del claim "role" (ruolo dell'utente)
     */
    public static String getRole() {
        return getJwt()
                .map(jwt -> jwt.getClaimAsString("role"))
                .orElseThrow(() -> new UnauthorizedException("Role dell'Utente non trovato"));
    }

    public static void checkIfAdmin() {
        String role = getRole();

        if (isNotAdmin(role)) {
            throw new UnauthorizedException("Non hai i permessi per eseguire questa operazione");
        }
    }

    public static void checkIfAdminOrGestore() {
        String role = getRole();

        if (isNotAdmin(role) && isNotGestore(role)) {
            throw new UnauthorizedException("Non hai i permessi per eseguire questa operazione");
        }
    }

    public static void checkIfUtenteAgenzia() {
        String role = getRole();

        if (!hasValidAgencyRole(role)) {
            throw new UnauthorizedException("Non hai i permessi per eseguire questa operazione");
        }
    }

    private static boolean isNotAdmin(String role) {
        return role == null || !role.equals("Admin");
    }

    private static boolean isNotGestore(String role) {
        return role == null || !role.equals("Gestore");
    }

    private static boolean hasValidAgencyRole(String role) {
        return role.equals("Admin") || role.equals("Gestore") || role.equals("AgenteImmobiliare");
    }
}
