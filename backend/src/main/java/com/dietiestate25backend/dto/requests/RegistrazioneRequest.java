package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.NotNull;

public class RegistrazioneRequest {
    @NotNull
    private String email;
    @NotNull
    private String password;
    @NotNull
    private String role;

    public RegistrazioneRequest() {}

    public RegistrazioneRequest(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}
