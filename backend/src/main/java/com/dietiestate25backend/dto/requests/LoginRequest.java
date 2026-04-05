package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email non può essere vuota")
    @Email(message = "Email non valida")
    @Size(max = 255, message = "Email non può superare i 255 caratteri")
    private String email;

    @NotBlank(message = "Password non può essere vuota")
    @Size(min=1, max=255, message="Password non valida")
    @Pattern(regexp = "^[\\x20-\\x7E]+$", message = "La password contiene caratteri non validi")
    private String password;
}