package com.dietiestate25backend.dao.modelinterface;

import com.dietiestate25backend.model.UtenteAgenzia;

public interface UtenteAgenziaDao {
    boolean save(UtenteAgenzia utente);
    int getIdAgenzia(String uid);
}
