package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.requests.MeteoRequest;
import com.dietiestate25backend.service.MeteoService;
import com.dietiestate25backend.utils.TokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/meteo")
public class MeteoController {
    private final MeteoService meteoService;

    public MeteoController(MeteoService meteoService) {
        this.meteoService = meteoService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> ottieniPrevisioni(@RequestHeader("Authorization") String token, @RequestBody MeteoRequest meteoRequest) {

        TokenUtils.validateToken(token);

        String latitudine = meteoRequest.getLatitudine();
        String longitudine = meteoRequest.getLongitudine();
        String date = meteoRequest.getDate();

        // Verifichiamo se la data è nel range
        if (!meteoService.isDataNelRange(date)) {
            return ResponseEntity.badRequest().body("Le previsioni meteo non sono disponibili per la data selezionata.");
        }

        // Ottieniamo i dati meteo
        try {
            Map<String, Object> previsioni = meteoService.ottieniPrevisioni(latitudine, longitudine, date);
            return ResponseEntity.ok(previsioni);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }