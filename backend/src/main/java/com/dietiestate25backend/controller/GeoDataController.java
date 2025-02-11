package com.dietiestate25backend.controller;

import com.dietiestate25backend.dto.requests.ConteggioPuntiInteresseRequest;
import com.dietiestate25backend.service.GeoDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/geodata")
public class GeoDataController {
    private final GeoDataService geoDataService;

    public GeoDataController(GeoDataService geoDataService) {
        this.geoDataService = geoDataService;
    }

    @PostMapping("/conteggio-pdi")
    public ResponseEntity<Map<String, Integer>> ottieniConteggioPuntiInteresse(@RequestBody ConteggioPuntiInteresseRequest conteggioPuntiInteresseRequest) {
        Map<String, Integer> conteggioPuntiInteresse = geoDataService.ottieniConteggioPuntiInteresse(conteggioPuntiInteresseRequest);
        
        return ResponseEntity.status(201).body(conteggioPuntiInteresse);
    }
}