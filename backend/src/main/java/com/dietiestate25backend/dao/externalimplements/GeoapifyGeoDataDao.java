@Repository
public class GeoapifyGeoDataDao implements GeoDataDao {
    private final RestTemplate restTemplate;

    // Dobbiamo creare un account Geoapify e specificare la chiave fornita da Geoapify qui dentro. Ovviamente usiamo un file .env
    private final String apiKey = "";

    public GeoapifyGeoDataDao(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public GeoDataResponse ottieniDatiGeografici(double latitudine, double longitudine, int raggio, List<String> categorie) {
        String urlTemplate = "https://api.geoapify.com/v2/places?categories=%s&filter=circle:%f,%f,%d&apiKey=%s";

        // Effettuiamo il mapping delle categorie generiche specificate dal front-end nella loro equivalente Geoapify
        Map<String, String> mappaCategorie = creaMappaCategorie();

        // Mappa in cui vengono salvate le occorrenze per ogni categoria specificata
        Map<String, Integer> risultati = new HashMap<>();

        // Facciamo per ogni categoria una chiamata API che ci restituisca le occorrenze per quella categoria.
        for (String categoria : categorie) {
            String categoriaGeoapify = mappaCategorie.getOrDefault(categoria.toLowerCase(), "");

            // Controlliamo che la categoria specificata sia valida
            if (categoriaGeoapify.isEmpty()) {
                throw new IllegalArgumentException("Categoria non supportata: " + categoria);
            }

            // Costruiamo l'URL per la chiamata API
            String url = String.format(
                urlTemplate,
                categoriaGeoapify,
                longitudine,
                latitudine,
                raggio,
                apiKey
            );

            // Effettuiamo la chiamata API e salviamo le occorrenze in un attributo
            try {
                GeoapifyResponse response = restTemplate.getForObject(url, GeoapifyResponse.class);
                int conteggio = response != null && response.getFeatures() != null ? response.getFeatures().size() : 0;

                risultati.put(categoria.toLowerCase(), conteggio);
            } catch (RestClientException e) {
                throw new RuntimeException("Errore nel chiamare Geoapify per categoria: " + categoria, e);
            }
        }

        return new GeoDataResponse(risultati);
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
}
