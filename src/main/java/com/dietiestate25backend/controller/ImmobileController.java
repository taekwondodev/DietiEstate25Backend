package com.dietiestate25backend.controller;

import com.dietiestate25backend.config.TokenUtils;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Indirizzo;
import com.dietiestate25backend.model.TipoClasseEnergetica;
import com.dietiestate25backend.service.ImmobileService;
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
    public ResponseEntity<List<Immobile>>  cercaImmobili(
            @RequestHeader("Authorization") String token,
            @RequestParam Indirizzo indirizzo, @RequestParam (required = false) Double prezzoMin,
            @RequestParam (required = false) Double prezzoMax, @RequestParam (required = false) String nStanze,
            @RequestParam (required = false) String tipologia, @RequestParam (required = false) TipoClasseEnergetica classeEnergetica
    ) {
        immobileService.validateToken(token);
        List<Immobile> immobili = immobileService.cercaImmobili(indirizzo, prezzoMin, prezzoMax, nStanze, tipologia, classeEnergetica);

        return ResponseEntity.status(201).body(immobili);
    }

    @PostMapping("/crea")
    public ResponseEntity<Void> creaImmobile(@RequestHeader("Authorization") String token, @RequestBody Immobile immobile) {
        String uid = TokenUtils.getUidFromToken(token);
        immobile.setIdResponsabile(UUID.fromString(uid));
        immobileService.creaImmobile(immobile);

        return ResponseEntity.status(201).build();
    }
}
