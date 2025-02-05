package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.NotNull;

public class RegistrazioneRequest {
    @NotNull
    private String email;
    @NotNull
    private String role;

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
