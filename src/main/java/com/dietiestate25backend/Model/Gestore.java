package com.dietiestate25backend.Model;

public class Gestore extends Utente{
    /// riferimento all'agenzia di appartenenza

    Gestore(String email, String password){
        this.email = email;
        this.password = password;
    }
}
