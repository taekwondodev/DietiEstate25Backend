package com.dietiestate25backend.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dietiestate25backend.error.exception.UnauthorizedException;

import java.util.Date;

public class TokenUtils {

    private TokenUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static DecodedJWT validateToken(String token) {
        DecodedJWT jwt = JWT.decode(token);

        if (jwt.getExpiresAt().before(new Date())) {
            throw new UnauthorizedException("Token scaduto o invalido");
        }
        else {
            return jwt;
        }
    }

    public static String getUidFromToken(String idToken) {
        DecodedJWT jwt = validateToken(idToken);
        return jwt.getSubject();
    }
}
