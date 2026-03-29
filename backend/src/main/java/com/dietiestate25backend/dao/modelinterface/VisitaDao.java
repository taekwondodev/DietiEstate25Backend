package com.dietiestate25backend.dao.modelinterface;

import com.dietiestate25backend.model.StatoVisita;
import com.dietiestate25backend.model.Visita;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

public interface VisitaDao {
    boolean salva(Date data, Time time, StatoVisita stato, String uidCliente, int idImmobile);
    boolean aggiornaStato(Visita visita);
    Visita getVisitaById(int id);
    List<Visita> riepilogoVisiteCliente(String idCliente);
    List<Visita> riepilogoVisiteUtenteAgenzia(String idAgente);
}
