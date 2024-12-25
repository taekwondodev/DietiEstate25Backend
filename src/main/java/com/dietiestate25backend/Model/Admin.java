package com.dietiestate25backend.Model;

public class Admin extends Utente{
    /// riferimento all'agenzia di appartenenza

    Admin(String email, String password){
        this.email = email;
        this.password = password;
    }
}
