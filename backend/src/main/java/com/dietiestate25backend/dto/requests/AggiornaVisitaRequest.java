package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;

public class AggiornaVisitaRequest {
    @Positive(message = "ID visita deve essere positivo")
    private int idVisita;

    @NotBlank(message = "Stato non può essere vuoto")
    @Pattern(regexp = "^(Confermata|Rifiutata|In sospeso)$",
            message = "Stato non valido. Permessi: Confermata, Rifiutata, In sospeso")
    private String stato;

    public AggiornaVisitaRequest() {}

    public AggiornaVisitaRequest(int idVisita, String stato) {
        this.idVisita = idVisita;
        this.stato = stato;
    }

    public int getIdVisita() {
        return idVisita;
    }

    public void setIdVisita(int idVisita) {
        this.idVisita = idVisita;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}
