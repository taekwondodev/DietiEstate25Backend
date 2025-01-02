package com.dietiestate25backend.model;

public class Immobile {
    private double prezzo;
    private String nStanze;
    private String tipologia;
    private Indirizzo indirizzo;
    private TipoClasseEnergetica classeEnergetica;
    private int idResponsabile;

    public Immobile(double prezzo, String nStanze, String tipologia, Indirizzo indirizzo, TipoClasseEnergetica classeEnergetica, int idResponsabile) {
        this.prezzo = prezzo;
        this.nStanze = nStanze;
        this.tipologia = tipologia;
        this.indirizzo = indirizzo;
        this.classeEnergetica = classeEnergetica;
        this.idResponsabile = idResponsabile;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public String getnStanze() {
        return nStanze;
    }

    public String getTipologia() {
        return tipologia;
    }

    public Indirizzo getIndirizzo() {
        return indirizzo;
    }

    public TipoClasseEnergetica getClasseEnergetica() {
        return classeEnergetica;
    }

    public int getIdResponsabile() {
        return idResponsabile;
    }

    public void setIdResponsabile(int idResponsabile) {
        this.idResponsabile = idResponsabile;
    }
}
