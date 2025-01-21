package main.java.com.dietiestate25backend.controller;

@RestController
@RequestMapping("/api/weather")
public class MeteoController {
    private final MeteoService meteoService;

    public MeteoController(meteoService meteoService) {
        this.meteoService = meteoService;
    }

    @GetMapping
    public ResponseEntity<?> getWeatherForDate(
        @RequestParam String city,
        @RequestParam String date
    ) {

        // Se la data scelta è fuori dal range delle previsioni meteo, allora restituisci avviso
        if (!meteoService.isDateInRange(date)) {
            return ResponseEntity.badRequest()
                .body("Le previsioni meteo non sono disponibili per la data selezionata.");
        }

        // Ricava meteo solo per quella data
        MeteoResponse meteo = meteoService.getWeatherForDate(city, date);
        return ResponseEntity.ok(meteo);
    }
}