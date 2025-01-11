package com.dietiestate25backend.dao.modelinterface;

import com.dietiestate25backend.model.Visita;

public interface VisitaDao {
    boolean salva(Visita visita);
    boolean aggiornaStato(Visita visita);
}
