@RestController
@RequestMapping("/api/geodati")
public class GeoDataController {
    private final GeoDataService geoDataService;

    public GeoDataController(GeoDataService geoDataService) {
        this.geoDataService = geoDataService;
    }

    @PostMapping
    public ResponseEntity<GeoDataResponse> ottieniGeoDati(@RequestBody GeoDataRequest geoDataRequest) {
        GeoDataResponse geoDataResponse = geoDataService.ottieniDatiGeografici(geoDataRequest);
        return ResponseEntity.ok(geoDataResponse);
    }
}