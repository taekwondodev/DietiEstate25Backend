package com.dietiestate25backend.dao.modelinterface;

import com.dietiestate25backend.model.Offerta;

import java.util.List;
import java.util.UUID;

public interface OffertaDao {
    boolean salvaOfferta(Offerta offerta);
    boolean aggiornaStatoOfferta(Offerta offerta);
    List<Offerta> riepilogoOfferteCliente(UUID idCliente);
    List<Offerta> riepilogoOfferteUteneAgenzia(UUID idAgente);
}
