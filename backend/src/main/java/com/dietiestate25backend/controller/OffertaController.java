package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.OffertaRequest;
import com.dietiestate25backend.service.OffertaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/offerta")
@Validated
public class OffertaController {
    private final OffertaService offertaService;

    public OffertaController(OffertaService offertaService) {
        this.offertaService = offertaService;
    }

    @PostMapping("/aggiungi")
    public ResponseEntity<Void> aggiungiOfferta(@RequestHeader("Authorization") String token, @Valid @RequestBody OffertaRequest request) {
        String uid = TokenUtils.getUidFromToken(token);
        request.setIdCliente(UUID.fromString(uid));

        offertaService.aggiungiOfferta(request);
        return ResponseEntity.status(201).build();
    }

    @PatchMapping("/aggiorna")
    public ResponseEntity<Void> aggiornaStatoOfferta(@RequestHeader("Authorization") String token, @Valid @RequestBody OffertaRequest request) {
        String uid = TokenUtils.getUidFromToken(token);
        request.setIdCliente(UUID.fromString(uid));

        offertaService.aggiornaStatoOfferta(request);
        return ResponseEntity.ok().build();
    }
}
