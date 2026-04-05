package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrenotaVisitaRequest {
    @Positive(message = "ID immobile deve essere positivo")
    @Max(value = 999999, message = "ID immobile non valido")
    private int idImmobile;

    @NotNull(message = "Data visita non può essere null")
    @FutureOrPresent(message = "Data visita deve essere oggi o nel futuro")
    private LocalDate dataVisita;

    @NotNull(message = "Ora visita non può essere null")
    private LocalTime oraVisita;
}
