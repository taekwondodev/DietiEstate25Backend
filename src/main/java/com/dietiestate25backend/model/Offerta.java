package com.dietiestate25backend.model;

public class Offerta {
    private double importo;
    private StatoOfferta stato;

    /// riferimento al cliente e immobile

    Offerta(double importo, StatoOfferta stato) {
        this.importo = importo;
        this.stato = stato;
    }
}
