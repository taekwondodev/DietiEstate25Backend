@Service
public class GeoDataService {
    private final GeoDataDao geoDataDao;

    @Autowired
    public GeoDataService(GeoDataDao geoDataDao) {
        this.geoDataDao = geoDataDao;
    }

    public Map<String, Integer> ottieniConteggioPuntiInteresse(ConteggioPuntiInteresseRequest conteggioPuntiInteresseRequest) {
        return geoDataDao.ottieniConteggioPuntiInteresse(
            conteggioPuntiInteresseRequest.getLatitudine(),
            conteggioPuntiInteresseRequest.getLongitudine(),
            conteggioPuntiInteresseRequest.getRaggio(),
            conteggioPuntiInteresseRequest.getCategorie()
        );
    }
}