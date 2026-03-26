package com.dietiestate25backend.model;

public class UtenteAgenzia {
    private final String uid;
    private final int idAgenzia;

    public UtenteAgenzia(int idAgenzia, String uid) {
        this.idAgenzia = idAgenzia;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public int getIdAgenzia() {
        return idAgenzia;
    }
}
