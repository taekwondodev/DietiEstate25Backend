package com.dietiestate25backend.model;

public enum StatoVisita {
    CONFERMATA("Confermata"),
    RIFIUTATA("Rifiutata"),
    IN_SOSPESO("In sospeso");

    private final String stato;

    StatoVisita(String stato) {
        this.stato = stato;
    }

    public String getStatoString() {
        return stato;
    }
}
