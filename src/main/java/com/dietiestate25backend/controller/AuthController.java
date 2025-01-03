package com.dietiestate25backend.controller;

import com.dietiestate25backend.model.Utente;
import com.dietiestate25backend.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /// chiamate http
    @PostMapping("/login")
    public boolean login(@RequestBody Utente utente) {
        return authService.login(utente);
    }

    @PostMapping("/registrazione")
    public boolean registrazione(@RequestBody Utente utente) {
        return authService.registrazione(utente);
    }
}
