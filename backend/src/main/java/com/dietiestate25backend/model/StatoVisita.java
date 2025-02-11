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

    public static StatoVisita fromString(String stato) {
        for (StatoVisita s : StatoVisita.values()) {
            if (s.getStatoString().equalsIgnoreCase(stato)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Stato visita non valida: " + stato);
    }
}
