package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class PrenotaVisitaRequest {
    @Positive(message = "ID immobile deve essere positivo")
    @Max(value = 999999, message = "ID immobile non valido")
    private int idImmobile;

    @NotNull(message = "Data visita non può essere null")
    @FutureOrPresent(message = "Data visita deve essere oggi o nel futuro")
    private LocalDate dataVisita;

    @NotNull(message = "Ora visita non può essere null")
    private LocalTime oraVisita;

    public PrenotaVisitaRequest() {}

    public PrenotaVisitaRequest(int idImmobile, LocalDate dataVisita, LocalTime oraVisita) {
        this.idImmobile = idImmobile;
        this.dataVisita = dataVisita;
        this.oraVisita = oraVisita;
    }

    public int getIdImmobile() {
        return idImmobile;
    }

    public void setIdImmobile(int idImmobile) {
        this.idImmobile = idImmobile;
    }

    public LocalDate getDataVisita() {
        return dataVisita;
    }

    public void setDataVisita(LocalDate dataVisita) {
        this.dataVisita = dataVisita;
    }

    public LocalTime getOraVisita() {
        return oraVisita;
    }

    public void setOraVisita(LocalTime oraVisita) {
        this.oraVisita = oraVisita;
    }
}
