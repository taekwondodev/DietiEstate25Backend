package com.dietiestate25backend.model;

import java.util.UUID;

public class Offerta {
    private final double importo;
    private final StatoOfferta stato;
    private UUID idCliente;
    private final int idImmobile;

    public Offerta(double importo, StatoOfferta stato, UUID idCliente, int idImmobile) {
        this.importo = importo;
        this.stato = stato;
        this.idCliente = idCliente;
        this.idImmobile = idImmobile;
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

    public int getIdImmobile() {
        return idImmobile;
    }
}
