package com.dietiestate25backend.dto;

import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.StatoOfferta;

import java.util.UUID;

public class OffertaRequest {
    private double importo;
    private StatoOfferta stato;
    private UUID idCliente;
    private Immobile immobile;

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

    public void setIdCliente(UUID idCliente) {
        this.idCliente = idCliente;
    }
}
