package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.NotNull;

public class CreaImmobileRequest {
    @NotNull
    private String descrizione;
    @NotNull
    private String urlFoto;
    @NotNull
    private double prezzo;
    @NotNull
    private int nBagni;
    @NotNull
    private int nStanze;
    @NotNull
    private String tipologia;
    @NotNull
    private String indirizzo;
    @NotNull
    private int piano;
    @NotNull
    private boolean hasAscensore;
    @NotNull
    private boolean hasBalcone;

    public String getDescrizione() {
        return descrizione;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public int getnBagni() {
        return nBagni;
    }

    public int getnStanze() {
        return nStanze;
    }

    public String getTipologia() {
        return tipologia;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public int getPiano() {
        return piano;
    }

    public boolean isHasAscensore() {
        return hasAscensore;
    }

    public boolean isHasBalcone() {
        return hasBalcone;
    }
}
