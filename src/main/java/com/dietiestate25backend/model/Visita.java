package com.dietiestate25backend.model;

import java.sql.Date;
import java.sql.Time;

public class Visita {
    private Date dataRichiesta;
    private Date dataVisita;
    private Time oraVisita;
    private StatoVisita stato;

    /// riferimento al cliente e immobile

    Visita(Date dataRichiesta, Date dataVisita, Time oraVisita, StatoVisita stato) {
        this.dataRichiesta = dataRichiesta;
        this.dataVisita = dataVisita;
        this.oraVisita = oraVisita;
        this.stato = stato;
    }
}
