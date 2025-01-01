package com.dietiestate25backend.model;

public class Immobile {
    private double prezzo;
    private String nStanze;
    private String tipologia;
    private Indirizzo indirizzo;
    private TipoClasseEnergetica classeEnergetica;

    /// riferimento all'agente immobiliare responsabile

    public Immobile(double prezzo, String nStanze, String tipologia, Indirizzo indirizzo, TipoClasseEnergetica classeEnergetica) {
        this.prezzo = prezzo;
        this.nStanze = nStanze;
        this.tipologia = tipologia;
        this.indirizzo = indirizzo;
        this.classeEnergetica = classeEnergetica;
    }
}
