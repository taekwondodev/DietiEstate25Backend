package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.requests.CreaImmobileRequest;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.service.ImmobileService;
import com.dietiestate25backend.utils.TokenUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Immobile>> cercaImmobili(
            @RequestParam String indirizzo,
            @RequestParam(required = false) Double prezzoMin,
            @RequestParam(required = false) Double prezzoMax, 
            @RequestParam(required = false) String nBagni,
            @RequestParam(required = false) String nStanze,
            @RequestParam(required = false) String tipologia
    ) {
    
        List<Immobile> immobili = immobileService.cercaImmobili(
            indirizzo, prezzoMin, prezzoMax, nBagni, nStanze, tipologia
        );
    
        return ResponseEntity.status(200).body(immobili);
    }

    @PostMapping("/crea")
    public ResponseEntity<Void> creaImmobile(@Valid @RequestBody CreaImmobileRequest request) {
        TokenUtils.checkIfUtenteAgenzia();
        String uidResponsabile = TokenUtils.getUserSub();
        immobileService.creaImmobile(request, uidResponsabile);

        return ResponseEntity.status(201).build();
    }
}
