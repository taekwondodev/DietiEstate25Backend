package com.dietiestate25backend.model;

public class AgenteImmobiliare extends Utente {
    /// riferimento all'agenzia di appartenenza

    public AgenteImmobiliare(String email, String password){
        this.email = email;
        this.password = password;
    }
}
