package com.dietiestate25backend.controller;

import com.dietiestate25backend.model.Offerta;
import com.dietiestate25backend.service.OffertaService;
import com.dietiestate25backend.utils.TokenUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<Void> aggiungiOfferta(@Valid @RequestBody Offerta request) {
        String uidCliente = TokenUtils.getUserSub();
        request.setIdCliente(UUID.fromString(uidCliente));

        offertaService.aggiungiOfferta(request);
        return ResponseEntity.status(201).build();
    }

    @PatchMapping("/aggiorna")
    public ResponseEntity<Void> aggiornaStatoOfferta(@Valid @RequestBody Offerta request) {
        String uidCliente = TokenUtils.getUserSub();
        request.setIdCliente(UUID.fromString(uidCliente));

        offertaService.aggiornaStatoOfferta(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/riepilogoCliente")
    public ResponseEntity<List<Offerta>> riepilogoOfferteCliente() {
        String uidCliente = TokenUtils.getUserSub();
        List<Offerta> offerte = offertaService.riepilogoOfferteCliente(UUID.fromString(uidCliente));

        return ResponseEntity.ok(offerte);
    }

    @GetMapping("/riepilogoUtenteAgenzia")
    public ResponseEntity<List<Offerta>> riepilogoOfferteUtenteAgenzia() {
        String idAgente = TokenUtils.getUserSub();
        List<Offerta> offerte = offertaService.riepilogoOfferteUtenteAgenzia(UUID.fromString(idAgente));

        return ResponseEntity.ok(offerte);
    }
}
