package com.dietiestate25backend.model;

public class UtenteAgenzia {
    private String uid;
    private int idAgenzia;
    private String ruolo;

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
