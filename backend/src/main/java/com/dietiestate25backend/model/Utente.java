package com.dietiestate25backend.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class Utente {
    private final String uid;
    private final String email;
    private final String password;
    private final String role;

    @Setter
    private int failedLoginAttempts;
    @Setter
    private Instant lockedUntil;

    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }
}