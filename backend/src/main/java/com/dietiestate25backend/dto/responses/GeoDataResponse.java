public class GeoDataResponse {

    /* 
     * Siccome la richiesta delle categorie di GeoDataRequest arriva in modo generalizzato tramite stringhe, allora anche la risposta
     * deve arrivare in modo generalizzato. Utilizziamo una mappa per mappare insieme il nome della categoria (String) e la quantità
     * presente (Integer)
    */
    private Map<String, Integer> risultati;

    public GeoDataResponse(Map<String, Integer> risultati) {
        this.risultati = risultati;
    }

    public Map<String, Integer> getRisultati() {
        return risultati;
    }

    public void setRisultati(Map<String, Integer> risultati) {
        this.risultati = risultati;
    }
}