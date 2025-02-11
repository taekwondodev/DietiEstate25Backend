package com.dietiestate25backend.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class Offerta {
    private int idOfferta;
    @Positive
    private final double importo;
    @NotNull
    private final StatoOfferta stato;
    private UUID idCliente;
    @NotNull
    private final Immobile immobile;

    public Offerta(int idOfferta, double importo, StatoOfferta stato, UUID idCliente, Immobile immobile) {
        this.idOfferta = idOfferta;
        this.importo = importo;
        this.stato = stato;
        this.idCliente = idCliente;
        this.immobile = immobile;
    }

    public double getImporto() {
        return importo;
    }

    public StatoOfferta getStato() {
        return stato;
    }

    public UUID getIdCliente() {
        return idCliente;
    }

    public Immobile getImmobile() {
        return immobile;
    }

    public int getIdOfferta() {
        return idOfferta;
    }

    public void setIdCliente(UUID idCliente) {
        this.idCliente = idCliente;
    }

    public void setIdOfferta(int idOfferta) {
        this.idOfferta = idOfferta;
    }
}
