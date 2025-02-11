package com.dietiestate25backend.model;

public enum StatoOfferta {
    ACCETTATA("Accettata"),
    RIFIUTATA("Rifiutata"),
    IN_SOSPESO("In sospeso");

    private final String stato;

    StatoOfferta(String stato) {
        this.stato = stato;
    }

    public String getStatoString() {
        return stato;
    }

    public static StatoOfferta fromString(String stato) {
        for (StatoOfferta s : StatoOfferta.values()) {
            if (s.getStatoString().equalsIgnoreCase(stato)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Stato offerta non valida: " + stato);
    }
}
