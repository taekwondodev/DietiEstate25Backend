public class GeoDataResponse {
    private int parchi;
    private int trasportiPubblici;
    private int scuole;
    private int ristoranti;

    public GeoDataResponse(int parchi, int trasportiPubblici, int scuole, int ristoranti) {
        this.parchi = parchi;
        this.trasportiPubblici = trasportiPubblici;
        this.scuole = scuole;
        this.ristoranti = ristoranti;
    }

    public int getParchi() {
        return parchi;
    }

    public void setParchi(int parchi) {
        this.parchi = parchi;
    }

    public int getTrasportiPubblici() {
        return trasportiPubblici;
    }

    public void setTrasportiPubblici(int trasportiPubblici) {
        this.trasportiPubblici = trasportiPubblici;
    }

    public int getScuole() {
        return scuole;
    }

    public void setScuole(int scuole) {
        this.scuole = scuole;
    }

    public int getRistoranti() {
        return ristoranti;
    }

    public void setRistoranti(int ristoranti) {
        this.ristoranti = ristoranti;
    }
}