package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.OffertaRequest;
import com.dietiestate25backend.service.OffertaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/offerta")
public class OffertaController {
    private final OffertaService offertaService;

    public OffertaController(OffertaService offertaService) {
        this.offertaService = offertaService;
    }

    @PostMapping("/aggiungi")
    public ResponseEntity<Void> aggiungiOfferta(@RequestHeader("Authorization") String token, @RequestBody OffertaRequest request) {
        String uid = offertaService.getUidFromToken(token);
        request.setIdCliente(UUID.fromString(uid));

        offertaService.aggiungiOfferta(request);
        return ResponseEntity.status(201).build();
    }
}
