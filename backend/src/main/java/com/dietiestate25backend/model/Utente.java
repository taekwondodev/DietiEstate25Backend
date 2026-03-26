package com.dietiestate25backend.model;

public class Utente {
    private final String uid;
    private final String email;
    private final String password;
    private final String role;

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
}
