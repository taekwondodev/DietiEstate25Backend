package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.requests.MeteoRequest;
import com.dietiestate25backend.service.MeteoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/meteo")
public class MeteoController {
    private final MeteoService meteoService;

    public MeteoController(MeteoService meteoService) {
        this.meteoService = meteoService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> ottieniPrevisioni(@Valid @RequestBody MeteoRequest meteoRequest) {
        Map<String, Object> previsioni = meteoService.ottieniPrevisioni(
                meteoRequest.getLatitudine(), meteoRequest.getLongitudine(), meteoRequest.getDate()
        );
        return ResponseEntity.ok(previsioni);
    }

}