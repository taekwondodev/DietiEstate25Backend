package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggiornaOffertaRequest {
    @Positive(message = "ID offerta deve essere positivo")
    private int idOfferta;

    @NotBlank(message = "Stato non può essere vuoto")
    private String stato;
}
