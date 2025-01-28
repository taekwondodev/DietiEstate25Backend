package com.dietiestate25backend.dto.requests;

import java.util.List;

public class ConteggioPuntiInteresseRequest {
    private double latitudine;
    private double longitudine;
    private int raggio;

    // La lista di categorie arriva sotto forma di lista di stringhe, così da generalizzare per qualsiasi Front-End
    private List<String> categorie;

    public ConteggioPuntiInteresseRequest(double latitudine, double longitudine, int raggio, List<String> categorie) {
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.raggio = raggio;
        this.categorie = categorie;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    public int getRaggio() {
        return raggio;
    }

    public void setRaggio(int raggio) {
        this.raggio = raggio;
    }

    public List<String> getCategorie() {
        return categorie;
    }

    public void setCategorie(List<String> categorie) {
        this.categorie = categorie;
    }
}