package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class RegistrazioneStaffRequest {
    @NotBlank(message = "Email non può essere vuota")
    @Email(message = "Email non valida")
    @Size(max = 254, message = "Email non può superare 254 caratteri")
    private String email;

    @NotBlank(message = "Password non può essere vuota")
    @Size(min=8, max=255, message = "La password deve essere tra 8 e 255 caratteri")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,255}$",
            message = "La password deve contenere almeno una lettera maiuscola, una lettera minuscola, un numero e un carattere speciale (@$!%*?&)"
    )
    private String password;

    @NotBlank(message = "Role non può essere vuoto")
    @Pattern(regexp = "^(AgenteImmobiliare|Gestore)$", message = "Role non valido. Permessi: AgenteImmobiliare, Gestore")
    private String role;

    public RegistrazioneStaffRequest() {}

    public RegistrazioneStaffRequest(String email, String password, String role) {
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

    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
}
