package main.java.com.dietiestate25backend.controller;

@RestController
@RequestMapping("/api/weather")
public class MeteoController {
    private final MeteoService meteoService;

    public MeteoController(MeteoService meteoService) {
        this.meteoService = meteoService;
    }

    @PostMapping
    public ResponseEntity<?> getMeteo(@RequestBody MeteoRequest meteoRequest) {
        // Spacchettiamo la MeteoRequest
        String city = meteoRequest.getCity();
        String date = meteoRequest.getDate();

        // Verifichiamo se la data è nel range delle previsioni meteo
        if (!meteoService.isDataNelRange(date)) {
            return ResponseEntity.badRequest()
                .body("Le previsioni meteo non sono disponibili per la data selezionata.");
        }

        // Una volta verificato che la data è nel range, impacchettiamo il meteo di quella data e quella località in una MeteoResponse
        MeteoResponse meteoResponse = meteoService.getMeteoPerData(city, date);

        // Restituisci la Response all'entità che fa la Request
        return ResponseEntity.ok(meteoResponse);
    }
}