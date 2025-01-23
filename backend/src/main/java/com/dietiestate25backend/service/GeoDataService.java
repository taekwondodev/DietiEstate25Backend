@Service
public class GeoDataService {
    private final GeoDataDao geoDataDao;

    @Autowired
    public GeoDataService(GeoDataDao geoDataDao) {
        this.geoDataDao = geoDataDao;
    }

    public GeoDataResponse ottieniDatiGeografici(GeoDataRequest geoDataRequest) {
        return geoDataDao.ottieniDatiGeografici(
            geoDataRequest.getLatitudine(),
            geoDataRequest.getLongitudine(),
            geoDataRequest.getRaggio(),
            geoDataRequest.getCategorie()
        );
    }
}