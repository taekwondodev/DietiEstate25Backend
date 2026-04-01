package com.dietiestate25backend.dao.externalimplements;

import com.dietiestate25backend.dao.modelinterface.GeoDataDao;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
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

    @Value("${GEO_KEY}")
    private String apiKey;

    public GeoapifyGeoDataDao(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public Map<String, Integer> ottieniConteggioPuntiInteresse(double latitudine, double longitudine, int raggio, List<String> categorie) {
        String urlTemplate = "https://api.geoapify.com/v2/places?categories=%s&filter=circle:%f,%f,%d&apiKey=%s";

        Map<String, String> mappaCategorie = creaMappaCategorie();
        Map<String, Integer> risultati = new HashMap<>();

        for (String categoriaGenerica : categorie) {
            String categoriaGeoapify = mappaCategorie.getOrDefault(categoriaGenerica.toLowerCase(), "");
            if (categoriaGeoapify.isEmpty()) {
                throw new BadRequestException(ErrorCode.INVALID_CATEGORY);
            }

            String url = String.format(
                urlTemplate,
                categoriaGeoapify,
                longitudine,
                latitudine,
                raggio,
                apiKey
            );

            try {
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                if (response == null || !response.containsKey("features")) {
                    throw new InternalServerErrorException(ErrorCode.EXTERNAL_SERVICE_ERROR);
                }

                List<?> features = (List<?>) response.get("features");
                int conteggio = features != null ? features.size() : 0;
                risultati.put(categoriaGenerica.toLowerCase(), conteggio);

            } catch (RestClientException e) {
                throw new InternalServerErrorException(ErrorCode.EXTERNAL_SERVICE_ERROR, e);
            }
        }

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
        return mappa;
    }

    @Override
    public Map<String, Double> ottieniCoordinate(String indirizzoCompleto) {
        String url = String.format(
            "https://api.geoapify.com/v1/geocode/search?text=%s&apiKey=%s",
            indirizzoCompleto.replace(" ", "%20"), apiKey
        );

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("features")) {
                throw new InternalServerErrorException(ErrorCode.EXTERNAL_SERVICE_ERROR);
            }

            List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");
            if (features.isEmpty()) {
                throw new BadRequestException(ErrorCode.ADDRESS_NOT_FOUND);
            }

            Map<String, Object> properties = (Map<String, Object>) features.get(0).get("properties");
            double latitudine = ((Number) properties.get("lat")).doubleValue();
            double longitudine = ((Number) properties.get("lon")).doubleValue();

            Map<String, Double> coordinate = new HashMap<>();
            coordinate.put("latitudine", latitudine);
            coordinate.put("longitudine", longitudine);
            return coordinate;

        } catch (BadRequestException | InternalServerErrorException e) {
            throw e;
        } catch (RestClientException e){
            throw new InternalServerErrorException(ErrorCode.EXTERNAL_SERVICE_ERROR, e);
        } catch(Exception e) {
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }
    }
}