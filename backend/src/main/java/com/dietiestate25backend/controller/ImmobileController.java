package com.dietiestate25backend.controller;

import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.service.ImmobileService;
import com.dietiestate25backend.utils.TokenUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/immobile")
public class ImmobileController {

    private final ImmobileService immobileService;

    public ImmobileController(ImmobileService immobileService) {
        this.immobileService = immobileService;
    }

    @GetMapping("/cerca")
    public ResponseEntity<List<Immobile>> cercaImmobili(
            @RequestParam String città,
            @RequestParam(required = false) String indirizzo, 
            @RequestParam(required = false) String numeroCivico, 
            @RequestParam(required = false) Double prezzoMin,
            @RequestParam(required = false) Double prezzoMax, 
            @RequestParam(required = false) String nStanze,
            @RequestParam(required = false) String tipologia, 
    ) {
    
        List<Immobile> immobili = immobileService.cercaImmobili(
            indirizzo, numeroCivico, città, prezzoMin, prezzoMax, nStanze, tipologia,
        );
    
        return ResponseEntity.status(200).body(immobili);
    }

    @PostMapping("/crea")
    public ResponseEntity<Void> creaImmobile(@RequestBody Immobile immobile) {
        TokenUtils.checkIfUtenteAgenzia();

        String uid = TokenUtils.getUidFromToken(token);
        immobile.setIdResponsabile(UUID.fromString(uid));
        immobileService.creaImmobile(immobile);

        return ResponseEntity.status(201).build();
    }
}
