package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.requests.LoginRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneStaffRequest;
import com.dietiestate25backend.dto.response.LoginResponse;
import com.dietiestate25backend.service.AuthService;
import com.dietiestate25backend.utils.TokenUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login con email e password, ritorna JWT
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Registrazione di un Cliente
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registraCliente(@Valid @RequestBody RegistrazioneRequest request) {
        authService.registraCliente(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Registrazione di un nuovo Gestore o AgenteImmobiliare (solo Admin)
     */
    @PostMapping("/register-staff")
    public ResponseEntity<Void> registraGestoreOrAgente(@Valid @RequestBody RegistrazioneStaffRequest request) {
        TokenUtils.checkIfAdminOrGestore();
        String uidAdmin = TokenUtils.getUserSub();

        authService.registraGestoreOrAgente(uidAdmin, request);
        return ResponseEntity.ok().build();
    }
}
