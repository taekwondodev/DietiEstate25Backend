public interface MeteoDao {
    Map<String, Object> ottieniPrevisioni(double latitudine, double longitudine, String data);
}