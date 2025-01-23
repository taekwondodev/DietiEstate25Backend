public class GeoDataRequest {
    private double latitudine;
    private double longitudine;
    private int raggio; // In metri
    private List<String> categorie; // Categorie richieste

    public GeoDataRequest(double latitudine, double longitudine, int raggio, List<String> categorie) {
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.raggio = raggio;
        this.categorie = categorie;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    public int getRaggio() {
        return raggio;
    }

    public void setRaggio(int raggio) {
        this.raggio = raggio;
    }

    public List<String> getCategorie() {
        return categorie;
    }

    public void setCategorie(List<String> categorie) {
        this.categorie = categorie;
    }
}