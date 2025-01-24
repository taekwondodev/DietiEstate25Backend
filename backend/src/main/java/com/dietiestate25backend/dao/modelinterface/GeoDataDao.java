public interface GeoDataDao {
    Map<String, Integer> ottieniConteggioPuntiInteresse(double latitudine, double longitudine, int raggio, List<String> categorie);
    Map<String, Double> ottieniCoordinate(String city);
}