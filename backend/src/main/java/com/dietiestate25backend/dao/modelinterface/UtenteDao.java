package com.dietiestate25backend.dao.modelinterface;

import com.dietiestate25backend.model.Utente;

public interface UtenteDao {
    boolean save(Utente u);
    void updateLoginAttempts(Utente u);
    Utente findByEmail(String email);
    String findEmailByUid(String uid);
}
