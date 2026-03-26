package com.dietiestate25backend.dao.modelinterface;

import com.dietiestate25backend.model.Offerta;

import java.util.List;

public interface OffertaDao {
    boolean salvaOfferta(Offerta offerta);
    boolean aggiornaStatoOfferta(Offerta offerta);
    List<Offerta> riepilogoOfferteCliente(String idCliente);
    List<Offerta> riepilogoOfferteUteneAgenzia(String idAgente);
}
