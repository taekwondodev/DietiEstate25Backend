public interface GeoDataDao {
    GeoDataResponse ottieniDatiGeografici(double latitudine, double longitudine, int raggio, List<String> categorie);
}