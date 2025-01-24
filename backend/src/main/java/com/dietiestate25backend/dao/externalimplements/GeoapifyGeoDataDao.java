@Repository
public class GeoapifyGeoDataDao implements GeoDataDao {
    private final RestTemplate restTemplate;

    // Dobbiamo creare un account Geoapify e specificare la chiave fornita da Geoapify qui dentro. Ovviamente usiamo un file .env
    private final String apiKey = "";

    public GeoapifyGeoDataDao(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public Map<String, Integer> ottieniConteggioPuntiInteresse(double latitudine, double longitudine, int raggio, List<String> categorie) {
        String urlTemplate = "https://api.geoapify.com/v2/places?categories=%s&filter=circle:%f,%f,%d&apiKey=%s";

        // Effettuiamo il mapping delle categorie generiche specificate dal front-end nella loro equivalente Geoapify
        Map<String, String> mappaCategorie = creaMappaCategorie();

        // Mappa in cui vengono salvate le occorrenze per ogni categoria specificata
        Map<String, Integer> risultati = new HashMap<>();

        // Facciamo per ogni categoria una chiamata API che ci restituisca le occorrenze per quella categoria.
        for (String categoriaGenerica : categorie) {
            // Traduciamo la categoria generica nella categoria specifica di Geoapify
            String categoriaGeoapify = mappaCategorie.getOrDefault(categoriaGenerica.toLowerCase(), "");
            if (categoriaGeoapify.isEmpty()) {
                throw new IllegalArgumentException("Categoria non supportata: " + categoriaGenerica);
            }

            // Costruiamo l'URL specifico per la categoria
            String url = String.format(
                urlTemplate,
                categoriaGeoapify,
                longitudine,
                latitudine,
                raggio,
                apiKey
            );

            try {
                // Effettuiamo la chiamata API e formalizziamo il risultato in una mappa
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                // Estraiamo la lista "features" dalla risposta
                List<?> features = (List<?>) response.get("features");

                // Contiamo gli oggetti trovati e aggiungiamoli alla mappa dei risultati usando il nome generico
                int conteggio = features != null ? features.size() : 0;
                risultati.put(categoriaGenerica.toLowerCase(), conteggio);

            } catch (RestClientException e) {
                throw new RuntimeException("Errore nel chiamare Geoapify per categoria: " + categoriaGenerica, e);
            }
        }

        // Restituiamo la mappa con i conteggi
        return risultati;
    }

    /*
     * HELPER METHOD: Questo metodo mappa le categorie generiche specificate nel front-end con la keyword di categoria 
     * specifica richiesta da Geoapify
    */
    private Map<String, String> creaMappaCategorie() {
        Map<String, String> mappa = new HashMap<>();
        mappa.put("parco", "leisure.park");
        mappa.put("trasporto", "transport.public");
        mappa.put("scuola", "education.school");
        //... in teoria questo deve essere completato con ulteriori categorie, ma al fine del nostro progetto non serve
        return mappa;
    }

    @Override
    public Map<String, Double> ottieniCoordinate(String city) {
        // Costruzione dell'URL per l'API Geoapify
        String url = String.format(
            "https://api.geoapify.com/v1/geocode/search?city=%s&apiKey=%s",
            city, apiKey
        );

        try {

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            // Estraiamo l'elenco di features dalla risposta
            List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");

            if (features == null || features.isEmpty()) {
                throw new RuntimeException("Non è stato possibile trovare le coordinate per la città: " + city);
            }

            // Ottieniamo le coordinate dal primo elemento delle "features"
            Map<String, Object> geometry = (Map<String, Object>) features.get(0).get("geometry");
            List<Double> coordinates = (List<Double>) geometry.get("coordinates");

            // Restituiamo latitudine e longitudine sottoforma di mappa
            return Map.of(
                "latitudine", coordinates.get(1),
                "longitudine", coordinates.get(0)
            );

        } catch (RestClientException e) {
            throw new RuntimeException("Errore durante la chiamata all'API Geoapify.", e);
        }
    }
}

