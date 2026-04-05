package com.dietiestate25backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;

@Getter
@AllArgsConstructor
public class Visita {
    @Setter
    private int idVisita;
    private final Date dataVisita;
    private final Time oraVisita;
    private final StatoVisita stato;
    @Setter
    private String idCliente;
    private final Immobile immobile;
}
