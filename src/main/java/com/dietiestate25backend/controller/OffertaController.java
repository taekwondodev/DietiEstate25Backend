package com.dietiestate25backend.controller;

import com.dietiestate25backend.model.Offerta;
import com.dietiestate25backend.service.OffertaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/offerta")
public class OffertaController {
    private final OffertaService offertaService;

    public OffertaController(OffertaService offertaService) {
        this.offertaService = offertaService;
    }

    @PostMapping("/aggiungi")
    public boolean aggiungiOfferta(@RequestHeader("Authorization") String token, @RequestBody Offerta offerta) {
        /// if token valido ok, altrimenti exception
        /// recuperare idCliente dal token
        /// recuperare idImmobile

        //return offertaService.aggiungiOfferta(offerta);
        return false;
    }
}
