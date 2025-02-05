package com.dietiestate25backend.dto.requests;

import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.StatoOfferta;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class OffertaRequest {
    @Min(0)
    private double importo;

    @NotNull
    private StatoOfferta stato;

    @NotNull
    private UUID idCliente;

    @NotNull
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
