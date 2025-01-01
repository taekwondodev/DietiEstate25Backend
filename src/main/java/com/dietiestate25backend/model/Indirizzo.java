package com.dietiestate25backend.model;

public class Indirizzo {
    private String via;
    private String comune;
    private String cap;

    public Indirizzo(String via, String comune, String cap){
        this.via = via;
        this.comune = comune;
        this.cap = cap;
    }

    public String getCap() {
        return cap;
    }
}
