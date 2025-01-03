package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.AggiornaPasswordRequest;
import com.dietiestate25backend.dto.LoginResponse;
import com.dietiestate25backend.dto.RegistrazioneRequest;
import com.dietiestate25backend.dto.RegistrazioneResponse;
import com.dietiestate25backend.service.AuthService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody RegistrazioneRequest request) {
        final LoginResponse response = authService.login(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/registrazione")
    public ResponseEntity<RegistrazioneResponse> registrazione(@RequestBody RegistrazioneRequest request) {
        final RegistrazioneResponse response = authService.registrazione(request);
        //posso salvarmi lo uid
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/aggiornaPassword")
    public ResponseEntity<Void> aggiornaPassword(@RequestBody AggiornaPasswordRequest request) {
        return authService.aggiornaPassword(request);
    }
}
