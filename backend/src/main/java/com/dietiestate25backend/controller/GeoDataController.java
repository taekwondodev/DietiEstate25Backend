@RestController
@RequestMapping("/geodata")
public class GeoDataController {
    private final GeoDataService geoDataService;

    public GeoDataController(GeoDataService geoDataService) {
        this.geoDataService = geoDataService;
    }

    @PostMapping("/conteggio-pdi")
    public ResponseEntity<Map<String, Integer>> ottieniConteggioPuntiInteresse(@RequestHeader("Authorization") String token, @RequestBody ConteggioPuntiInteresseRequest conteggioPuntiInteresseRequest) {
       
        TokenUtils.validateToken(token);
       
        Map<String, Integer> conteggioPuntiInteresse = geoDataService.ottieniConteggioPuntiInteresse(conteggioPuntiInteresseRequest);
        
        return ResponseEntity.status(201).body(conteggioPuntiInteresse);
    }
}