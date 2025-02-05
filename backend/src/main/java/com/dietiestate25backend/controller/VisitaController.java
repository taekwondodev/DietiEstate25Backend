package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.VisitaRequest;
import com.dietiestate25backend.service.VisitaService;
import com.dietiestate25backend.utils.TokenUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/visita")
@Validated
public class VisitaController {
    private final VisitaService visitaService;

    public VisitaController(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    @PostMapping("/prenota")
    public ResponseEntity<Void> prenotaVisita(@RequestHeader("Authorization") String token, @Valid @RequestBody VisitaRequest request) {
        String uid = TokenUtils.getUidFromToken(token);
        request.setIdCliente(UUID.fromString(uid));

        visitaService.prenotaVisita(request);
        return ResponseEntity.status(201).build();
    }

    @PatchMapping("/aggiorna")
    public ResponseEntity<Void> aggiornaStatoVisita(@RequestHeader("Authorization") String token, @Valid @RequestBody VisitaRequest request) {
        String uid = TokenUtils.getUidFromToken(token);
        request.setIdCliente(UUID.fromString(uid));

        visitaService.aggiornaStatoVisita(request);
        return ResponseEntity.ok().build();
    }
}
