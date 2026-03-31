package com.dietiestate25backend.model;

import java.time.Instant;

public class Utente {
    private final String uid;
    private final String email;
    private final String password;
    private final String role;

    private int failedLoginAttempts;
    private Instant lockedUntil;

    public Utente(String uid, String email, String password, String role) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() { return role; }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }
}
