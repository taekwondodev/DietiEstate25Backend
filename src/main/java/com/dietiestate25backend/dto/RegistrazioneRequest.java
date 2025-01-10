package com.dietiestate25backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrazioneRequest {
    @NotBlank(message = "Email non può essere vuota")
    @Email(message = "Email non è valida")
    private String email;

    @NotBlank(message = "Password non puo essere vuota")
    @Size(min = 8, message = "Password deve essere almeno di 8 caratteri")
    private String password;

    @NotBlank(message = "Gruppo non può essere vuoto")
    private String group;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getGroup() {
        return group;
    }
}
