package com.dietiestate25backend.model;

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
