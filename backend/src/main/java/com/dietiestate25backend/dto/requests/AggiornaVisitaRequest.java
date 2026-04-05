package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggiornaVisitaRequest {
    @Positive(message = "ID visita deve essere positivo")
    private int idVisita;

    @NotBlank(message = "Stato non può essere vuoto")
    @Pattern(regexp = "^(Confermata|Rifiutata|In sospeso)$",
            message = "Stato non valido. Permessi: Confermata, Rifiutata, In sospeso")
    private String stato;
}
