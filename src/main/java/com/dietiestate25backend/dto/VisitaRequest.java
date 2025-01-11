package com.dietiestate25backend.dto;

import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.StatoVisita;
import jakarta.validation.constraints.NotNull;

import java.sql.Date;
import java.sql.Time;
import java.util.UUID;

public class VisitaRequest {
    @NotNull
    private Date dataRichiesta;

    @NotNull
    private Date dataVisita;

    @NotNull
    private Time oraVisita;

    @NotNull
    private StatoVisita stato;

    @NotNull
    private UUID idCliente;

    @NotNull
    private Immobile immobile;

    public Date getDataRichiesta() {
        return dataRichiesta;
    }

    public Date getDataVisita() {
        return dataVisita;
    }

    public Time getOraVisita() {
        return oraVisita;
    }

    public StatoVisita getStato() {
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
