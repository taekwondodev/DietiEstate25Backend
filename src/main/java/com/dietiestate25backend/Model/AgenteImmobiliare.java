package com.dietiestate25backend.Model;

public class AgenteImmobiliare extends Utente {
    /// riferimento all'agenzia di appartenenza

    AgenteImmobiliare(String email, String password){
        this.email = email;
        this.password = password;
    }
}
