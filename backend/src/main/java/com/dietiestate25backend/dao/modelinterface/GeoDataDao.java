public interface GeoDataDao {
    ConteggioPuntiInteresseResponse ottieniDatiGeograficiPerCategoria(double latitudine, double longitudine, int raggio, List<String> categorie);
}