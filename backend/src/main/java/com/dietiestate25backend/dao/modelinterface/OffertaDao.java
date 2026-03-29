package com.dietiestate25backend.dao.modelinterface;

import com.dietiestate25backend.model.Offerta;
import com.dietiestate25backend.model.StatoOfferta;

import java.util.List;

public interface OffertaDao {
    boolean salvaOfferta(double importo, StatoOfferta stato, String uidCliente, int idImmobile);
    Offerta getOffertaById(int idOfferta);
    boolean aggiornaStatoOfferta(Offerta offerta);
    List<Offerta> riepilogoOfferteCliente(String idCliente);
    List<Offerta> riepilogoOfferteUteneAgenzia(String idAgente);
}
