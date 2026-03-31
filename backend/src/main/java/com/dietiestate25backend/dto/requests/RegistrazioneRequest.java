package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;

public class RegistrazioneRequest {
    @NotBlank
    @Email(message = "Email non valida")
    @Size(max = 254, message = "L'email non può superare i 254 caratteri")
    private String email;
    @NotBlank
    @Size(min=8, max=255, message = "La password deve essere tra 8 e 255 caratteri")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,255}$",
            message = "La password deve contenere almeno una lettera maiuscola, una lettera minuscola, un numero e un carattere speciale (@$!%*?&)"
    )
    private String password;
    @NotBlank
    @Pattern(regexp = "^(Cliente|Gestore|AgenteImmobiliare)$", message = "Il ruolo deve essere Cliente, Gestore o AgenteImmobiliare")
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
