package com.dietiestate25backend.dao.modelinterface;

import com.dietiestate25backend.model.Visita;

import java.util.List;
import java.util.UUID;

public interface VisitaDao {
    boolean salva(Visita visita);
    boolean aggiornaStato(Visita visita);
    List<Visita> riepilogoVisiteCliente(UUID idCliente);
    List<Visita> riepilogoVisiteUtenteAgenzia(UUID idAgente);
}
