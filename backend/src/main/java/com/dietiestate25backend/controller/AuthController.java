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
    public ResponseEntity<Void> registraGestoreOrAgente(@RequestHeader("Authorization") String token, @Valid @RequestBody RegistrazioneRequest request) {
        String uid = TokenUtils.getUidFromToken(token);
        authService.registraGestoreOrAgente(uid, request);

        return ResponseEntity.ok().build();
    }
}
