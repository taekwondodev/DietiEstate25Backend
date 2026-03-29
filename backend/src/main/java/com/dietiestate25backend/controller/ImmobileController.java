package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.requests.CreaImmobileRequest;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.service.ImmobileService;
import com.dietiestate25backend.utils.TokenUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/immobile")
@Validated
public class ImmobileController {

    private final ImmobileService immobileService;

    public ImmobileController(ImmobileService immobileService) {
        this.immobileService = immobileService;
    }

    @GetMapping("/cerca")
    public ResponseEntity<List<Immobile>> cercaImmobili(
            @RequestParam(defaultValue = "0")@Min(value=0, message="Page non può essere negativa") int page,
            @RequestParam(defaultValue = "5")@Positive(message="Size deve essere positivo") int size,
            @RequestParam @NotBlank(message="Comune non può essere vuoto") String comune,
            @RequestParam(required = false) String tipologia,
            @RequestParam(required = false) Double prezzoMin,
            @RequestParam(required = false) Double prezzoMax,
            @RequestParam(required = false) Double dimensione,
            @RequestParam(required = false) Integer nBagni
    ) {
    
        List<Immobile> response = immobileService.cercaImmobili(
            tipologia, comune, prezzoMin, prezzoMax, dimensione, nBagni, page, size
        );
    
        return ResponseEntity.status(200).body(response);
    }

    @GetMapping("/personali")
    public ResponseEntity<List<Immobile>> immobiliPersonali() {
        TokenUtils.checkIfUtenteAgenzia();
        String uidResponsabile = TokenUtils.getUserSub();
        List<Immobile> response = immobileService.immobiliPersonali(uidResponsabile);

        return ResponseEntity.status(200).body(response);
    }

    @PostMapping("/crea")
    public ResponseEntity<Void> creaImmobile(@Valid @RequestBody CreaImmobileRequest request) {
        TokenUtils.checkIfUtenteAgenzia();
        String uidResponsabile = TokenUtils.getUserSub();
        immobileService.creaImmobile(request, uidResponsabile);

        return ResponseEntity.status(201).build();
    }
}
