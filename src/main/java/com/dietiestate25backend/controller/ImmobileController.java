package com.dietiestate25backend.controller;

import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Indirizzo;
import com.dietiestate25backend.model.TipoClasseEnergetica;
import com.dietiestate25backend.service.ImmobileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam Indirizzo indirizzo, @RequestParam (required = false) Double prezzoMin,
            @RequestParam (required = false) Double prezzoMax, @RequestParam (required = false) String nStanze,
            @RequestParam (required = false) String tipologia, @RequestParam (required = false) TipoClasseEnergetica classeEnergetica
    ) {
        return immobileService.cercaImmobili(indirizzo, prezzoMin, prezzoMax, nStanze, tipologia, classeEnergetica);
    }

    /// post nuovo immobile
}
