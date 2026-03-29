package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.requests.AggiornaVisitaRequest;
import com.dietiestate25backend.dto.requests.PrenotaVisitaRequest;
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
    public ResponseEntity<Void> prenotaVisita(@Valid @RequestBody PrenotaVisitaRequest request) {
        String uidCliente = TokenUtils.getUserSub();
        visitaService.prenotaVisita(request, uidCliente);
        return ResponseEntity.status(201).build();
    }

    @PatchMapping("/aggiorna")
    public ResponseEntity<Void> aggiornaStatoVisita(@Valid @RequestBody AggiornaVisitaRequest request) {
        String uidCliente = TokenUtils.getUserSub();
        visitaService.aggiornaStatoVisita(request, uidCliente);
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
