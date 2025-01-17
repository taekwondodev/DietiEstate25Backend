package com.dietiestate25backend.model;

import java.sql.Date;
import java.sql.Time;
import java.util.UUID;

public class Visita {
    private final Date dataRichiesta;
    private final Date dataVisita;
    private final Time oraVisita;
    private final StatoVisita stato;
    private final UUID idCliente;
    private final int idImmobile;

    public Visita(Date dataRichiesta, Date dataVisita, Time oraVisita, StatoVisita stato, UUID idCliente, int idImmobile) {
        this.dataRichiesta = dataRichiesta;
        this.dataVisita = dataVisita;
        this.oraVisita = oraVisita;
        this.stato = stato;
        this.idCliente = idCliente;
        this.idImmobile = idImmobile;
    }

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

    public int getIdImmobile() {
        return idImmobile;
    }
}
