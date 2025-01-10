package com.dietiestate25backend.model;

public class Indirizzo {
    private final String via;
    private final String comune;
    private final String cap;

    public Indirizzo(String via, String comune, String cap){
        this.via = via;
        this.comune = comune;
        this.cap = cap;
    }

    public String getCap() {
        return cap;
    }

    public String getVia() {
        return via;
    }

    public String getComune() {
        return comune;
    }
}
