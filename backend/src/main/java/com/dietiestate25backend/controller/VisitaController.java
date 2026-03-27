package com.dietiestate25backend.controller;

import com.dietiestate25backend.model.Visita;
import com.dietiestate25backend.service.VisitaService;
import com.dietiestate25backend.utils.TokenUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/visita")
@Validated
public class VisitaController {
    private final VisitaService visitaService;

    public VisitaController(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    @PostMapping("/prenota")
    public ResponseEntity<Void> prenotaVisita(@Valid @RequestBody Visita request) {
        String uidCliente = TokenUtils.getUserSub();
        request.setIdCliente(uidCliente);

        visitaService.prenotaVisita(request);
        return ResponseEntity.status(201).build();
    }

    @PatchMapping("/aggiorna")
    public ResponseEntity<Void> aggiornaStatoVisita(@Valid @RequestBody Visita request) {
        String uidCliente = TokenUtils.getUserSub();
        request.setIdCliente(uidCliente);

        visitaService.aggiornaStatoVisita(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/riepilogoCliente")
    public ResponseEntity<List<Visita>> riepilogoVisiteCliente() {
        String uidCliente = TokenUtils.getUserSub();
        List<Visita> visite = visitaService.riepilogoVisiteCliente(uidCliente);

        return ResponseEntity.ok(visite);
    }

    @GetMapping("/riepilogoUtenteAgenzia")
    public ResponseEntity<List<Visita>> riepilogoVisitaUtenteAgenzia() {
        TokenUtils.checkIfUtenteAgenzia();
        String idResponsabile = TokenUtils.getUserSub();
        List<Visita> visite = visitaService.riepilogoVisiteUtenteAgenzia(idResponsabile);

        return ResponseEntity.ok(visite);
    }
}
