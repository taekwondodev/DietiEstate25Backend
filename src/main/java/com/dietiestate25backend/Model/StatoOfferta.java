package com.dietiestate25backend.Model;

public enum StatoOfferta {
    ACCETTATA("Accettata"),
    RIFIUTATA("Rifiutata"),
    IN_SOSPESO("In sospeso");

    private final String stato;

    StatoOfferta(String stato) {
        this.stato = stato;
    }

    public String getStato() {
        return stato;
    }
}
