package com.dietiestate25backend.controller;

import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Indirizzo;
import com.dietiestate25backend.model.TipoClasseEnergetica;
import com.dietiestate25backend.service.ImmobileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/immobile")
public class ImmobileController {

    private final ImmobileService immobileService;

    public ImmobileController(ImmobileService immobileService) {
        this.immobileService = immobileService;
    }

    @GetMapping("/cerca")
    public List<Immobile> cercaImmobili(
            @RequestHeader("Authorization") String token,
            @RequestParam Indirizzo indirizzo, @RequestParam (required = false) Double prezzoMin,
            @RequestParam (required = false) Double prezzoMax, @RequestParam (required = false) String nStanze,
            @RequestParam (required = false) String tipologia, @RequestParam (required = false) TipoClasseEnergetica classeEnergetica
    ) {
        immobileService.validateToken(token);
        return immobileService.cercaImmobili(indirizzo, prezzoMin, prezzoMax, nStanze, tipologia, classeEnergetica);
    }

    @PostMapping("/crea")
    public boolean creaImmobile(@RequestHeader("Authorization") String token, @RequestBody Immobile immobile) {
        /// if token valido ok, altrimenti exception
        /// recuperare idAgente dal token
        /// chiamare immobile.setIdResponsabile(idAgente)
        return immobileService.creaImmobile(immobile);
    }
}
