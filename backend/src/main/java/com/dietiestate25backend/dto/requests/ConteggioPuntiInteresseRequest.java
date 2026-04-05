package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConteggioPuntiInteresseRequest {
    @NotNull(message = "Latitudine non può essere null")
    @DecimalMin(value = "-90.0", message = "Latitudine deve essere >= -90")
    @DecimalMax(value = "90.0", message = "Latitudine deve essere <= 90")
    private Double latitudine;

    @NotNull(message = "Longitudine non può essere null")
    @DecimalMin(value = "-180.0", message = "Longitudine deve essere >= -180")
    @DecimalMax(value = "180.0", message = "Longitudine deve essere <= 180")
    private Double longitudine;

    @Positive(message = "Raggio deve essere positivo")
    @Max(value = 50000, message = "Raggio non può superare 50000 metri")
    private Integer raggio;

    @NotNull(message = "Categorie non può essere null")
    @NotEmpty(message = "Categorie non può essere vuota")
    @Size(min = 1, max = 10, message = "Categorie deve contenere tra 1 e 10 elementi")
    private List<String> categorie;
}
