package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
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

    @PostMapping("/register")
    public ResponseEntity<Void> registraGestoreOrAgente(@Valid @RequestBody RegistrazioneRequest request) {
        TokenUtils.checkIfAdmin();

        String uidAdmin = TokenUtils.getUserSub();
        authService.registraGestoreOrAgente(uidAdmin, request);

        return ResponseEntity.ok().build();
    }
}
