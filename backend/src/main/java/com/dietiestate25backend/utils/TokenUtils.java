package com.dietiestate25backend.utils;

import com.dietiestate25backend.error.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Optional;

public class TokenUtils {

    private static final Logger logger = LoggerFactory.getLogger(TokenUtils.class);

    private TokenUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Ottiene il token JWT dell'utente autenticato
     */
    private static Optional<Jwt> getJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            logger.info("JWT trovato");
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

    public static void checkIfAdmin() {
        String role = getJwt()
                .map(jwt -> jwt.getClaimAsString("custom:role"))
                .orElse(null);

        logger.info("Ruolo estratto dal token JWT per verificare admin: {}", role);

        if (isNotAdmin(role)) {
            throw new UnauthorizedException("Non hai i permessi per eseguire questa operazione");
        }
    }

    public static void checkIfUtenteAgenzia() {
        String role = getJwt()
                .map(jwt -> jwt.getClaimAsString("custom:role"))
                .orElse(null);

        logger.info("Ruolo estratto dal token JWT per verificare utenteagenzia: {}", role);

        if (isNotAdmin(role) && isNotGestore(role) && isNotAgente(role)) {
            throw new UnauthorizedException("Non hai i permessi per eseguire questa operazione");
        }
    }

    private static boolean isNotAdmin(String role) {
        return role == null || !role.equals("Admin");
    }

    private static boolean isNotGestore(String role) {
        return role == null || !role.equals("Gestore");
    }

    private static boolean isNotAgente(String role) {
        return role == null || !role.equals("AgenteImmobiliare");
    }
}
