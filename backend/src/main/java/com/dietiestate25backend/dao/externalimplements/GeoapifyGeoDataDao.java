package com.dietiestate25backend.dao.externalimplements;

import com.dietiestate25backend.dao.modelinterface.GeoDataDao;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.error.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class GeoapifyGeoDataDao implements GeoDataDao {
    private final RestTemplate restTemplate;

    // Dobbiamo creare un account Geoapify e specificare la chiave fornita da Geoapify qui dentro. Ovviamente usiamo un file .env
    @Value("${GEO_KEY}")
    private String apiKey;

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
                throw new BadRequestException("Categoria non supportata: " + categoriaGenerica);
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

                if (response == null || !response.containsKey("features")) {
                    throw new NotFoundException("Geoapify non ha restituito dati validi.");
                }

                // Estraiamo la lista "features" dalla risposta
                List<?> features = (List<?>) response.get("features");

                // Contiamo gli oggetti trovati e aggiungiamoli alla mappa dei risultati usando il nome generico
                int conteggio = features != null ? features.size() : 0;
                risultati.put(categoriaGenerica.toLowerCase(), conteggio);

            } catch (RestClientException e) {
                throw new InternalServerErrorException("Errore nel chiamare Geoapify per categoria: " + categoriaGenerica, e);
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
        mappa.put("trasporto", "public_transport");
        mappa.put("scuola", "education.school");
        //... in teoria questo deve essere completato con ulteriori categorie, ma al fine del nostro progetto non serve
        return mappa;
    }

    @Override
    public Map<String, Double> ottieniCoordinate(String indirizzoCompleto) {
        // Sostituiamo gli spazi con "%20" per l'URL encoding
        String url = String.format(
            "https://api.geoapify.com/v1/geocode/search?text=%s&apiKey=%s",
            indirizzoCompleto.replace(" ", "%20"), apiKey
        );

        try {
            // Effettuiamo la chiamata API
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("features")) {
                throw new RuntimeException("Geoapify non ha restituito dati validi.");
            }

            List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");
            if (features.isEmpty()) {
                throw new RuntimeException("Nessuna coordinata trovata per l'indirizzo fornito.");
            }

            // Estraiamo la latitudine e la longitudine dal JSON ottenuto dalla chiamata API
            Map<String, Object> geometry = (Map<String, Object>) features.get(0).get("geometry");
            double latitudine = (double) geometry.get("lat");
            double longitudine = (double) geometry.get("lon");

            // Creiamo una mappa con le coordinate
            Map<String, Double> coordinate = new HashMap<>();
            coordinate.put("latitudine", latitudine);
            coordinate.put("longitudine", longitudine);
            return coordinate;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la chiamata a Geoapify.", e);
        }
    }
}

