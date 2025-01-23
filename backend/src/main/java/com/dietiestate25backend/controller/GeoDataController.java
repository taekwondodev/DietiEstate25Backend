@RestController
@RequestMapping("/geodata")
public class GeoDataController {
    private final GeoDataService geoDataService;

    public GeoDataController(GeoDataService geoDataService) {
        this.geoDataService = geoDataService;
    }

    @PostMapping("/conteggio-pdi")
    public ResponseEntity<?> ottieniConteggioPuntiInteresse(@RequestBody ConteggioPuntiInteresseRequest conteggioPuntiInteresseRequest) {
        Map<String, Integer> conteggioPuntiInteresse = geoDataService.ottieniConteggioPuntiInteresse(conteggioPuntiInteresseRequest);
        return ResponseEntity.status(201).body(conteggioPuntiInteresse);
    }
}