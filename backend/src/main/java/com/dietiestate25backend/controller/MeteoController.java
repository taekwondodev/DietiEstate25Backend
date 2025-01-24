package main.java.com.dietiestate25backend.controller;

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

        String city = meteoRequest.getCity();
        String date = meteoRequest.getDate();

        // Verifichiamo se la data è nel range
        if (!meteoService.isDataNelRange(date)) {
            return ResponseEntity.badRequest().body("Le previsioni meteo non sono disponibili per la data selezionata.");
        }

        // Ottieniamo i dati meteo
        try {
            Map<String, Object> previsioni = meteoService.ottieniPrevisioni(city, date);
            return ResponseEntity.ok(previsioni);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }