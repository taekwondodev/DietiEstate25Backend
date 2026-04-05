package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreaOffertaRequest {
    @Positive(message = "Importo deve essere positivo")
    @DecimalMax(value = "999999999.99", message = "Importo troppo grande")
    private double importo;

    @Positive(message = "ID immobile deve essere positivo")
    @Max(value = 999999, message = "ID immobile non valido")
    private int idImmobile;
}
