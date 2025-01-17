package com.dietiestate25backend.dao.modelinterface;

import com.dietiestate25backend.model.Offerta;

public interface OffertaDao {
    boolean salvaOfferta(Offerta offerta);
    boolean aggiornaStatoOfferta(Offerta offerta);
}
