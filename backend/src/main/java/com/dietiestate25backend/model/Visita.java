package com.dietiestate25backend.model;

import java.sql.Date;
import java.sql.Time;
import java.util.UUID;

public class Visita {
    private int idVisita;
    private final Date dataVisita;
    private final Time oraVisita;
    private final StatoVisita stato;
    private String idCliente;
    private final Immobile immobile;

    public Visita(int idVisita, Date dataVisita, Time oraVisita, StatoVisita stato, String idCliente, Immobile immobile) {
        this.idVisita = idVisita;
        this.dataVisita = dataVisita;
        this.oraVisita = oraVisita;
        this.stato = stato;
        this.idCliente = idCliente;
        this.immobile = immobile;
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

    public String getIdCliente() {
        return idCliente;
    }

    public Immobile getImmobile() {
        return immobile;
    }

    public int getIdVisita() {
        return idVisita;
    }

    public void setIdVisita(int idVisita) {
        this.idVisita = idVisita;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }
}
