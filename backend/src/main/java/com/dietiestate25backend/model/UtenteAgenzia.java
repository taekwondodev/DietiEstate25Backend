package com.dietiestate25backend.model;

public class UtenteAgenzia {
    private final String uid;
    private final int idAgenzia;
    private final String ruolo;

    public UtenteAgenzia(String ruolo, int idAgenzia, String uid) {
        this.ruolo = ruolo;
        this.idAgenzia = idAgenzia;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public int getIdAgenzia() {
        return idAgenzia;
    }

    public String getRuolo() {
        return ruolo;
    }
}
