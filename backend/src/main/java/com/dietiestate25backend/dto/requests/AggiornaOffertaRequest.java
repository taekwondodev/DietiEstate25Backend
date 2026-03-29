package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;

public class AggiornaOffertaRequest {
    @Positive(message = "ID offerta deve essere positivo")
    private int idOfferta;

    @NotBlank(message = "Stato non può essere vuoto")
    private String stato;

    public AggiornaOffertaRequest() {}

    public AggiornaOffertaRequest(int idOfferta, String stato) {
        this.idOfferta = idOfferta;
        this.stato = stato;
    }

    public int getIdOfferta() {
        return idOfferta;
    }

    public void setIdOfferta(int idOfferta) {
        this.idOfferta = idOfferta;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}
