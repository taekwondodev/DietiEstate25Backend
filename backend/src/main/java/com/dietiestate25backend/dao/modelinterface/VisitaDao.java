package com.dietiestate25backend.dao.modelinterface;

import com.dietiestate25backend.model.Visita;

import java.util.List;

public interface VisitaDao {
    boolean salva(Visita visita);
    boolean aggiornaStato(Visita visita);
    List<Visita> riepilogoVisiteCliente(String idCliente);
    List<Visita> riepilogoVisiteUtenteAgenzia(String idAgente);
}
