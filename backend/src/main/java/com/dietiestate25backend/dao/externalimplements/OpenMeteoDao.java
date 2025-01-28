package com.dietiestate25backend.dao.externalimplements;

import com.dietiestate25backend.dao.modelinterface.MeteoDao;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OpenMeteoDao implements MeteoDao {
    private final RestTemplate restTemplate;

    public OpenMeteoDao(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public Map<String, Object> ottieniPrevisioni(double latitudine, double longitudine, String data) {
        // Costruiamo dell'URL per la richiesta API
        String url = String.format(
            "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&daily=temperature_2m_max,temperature_2m_min,weathercode&timezone=auto",
            latitudine, longitudine
        );

        try {
            // Effettuiamo la chiamata API
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            // Parsing della risposta per estrarre i dati necessari
            Map<String, Object> daily = (Map<String, Object>) response.get("daily");
            List<String> dates = (List<String>) daily.get("time");
            List<Double> temperatureMax = (List<Double>) daily.get("temperature_2m_max");
            List<Double> temperatureMin = (List<Double>) daily.get("temperature_2m_min");
            List<Integer> weatherCodes = (List<Integer>) daily.get("weathercode");

            // Troviamo l'indice corrispondente alla data
            int index = dates.indexOf(data);
            if (index == -1) {
                throw new RuntimeException("Data non trovata nelle previsioni meteo.");
            }

            // Otteniamo i dati solo per la data richiesta
            double tempMax = temperatureMax.get(index);
            double tempMin = temperatureMin.get(index);
            int weatherCode = weatherCodes.get(index);
            String weatherDescription = descriviTempo(weatherCode);

            // Inseriamo i risultati in una mappa
            Map<String, Object> risultati = new HashMap<>();
            risultati.put("temperature_max", tempMax);
            risultati.put("temperature_min", tempMin);
            risultati.put("weather_description", weatherDescription);
            return risultati;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la chiamata all'API OpenMeteo.", e);
        }
    }

    /**
     * HELPER METHOD: Converte il weatherCode ricevuto dalla chiamata API in una stringa che descrive il tempo.
     */
    private String descriviTempo(int weatherCode) {
        switch (weatherCode) {
            case 0: return "Sereno";
            case 1: 
            case 2: 
            case 3: return "Parzialmente nuvoloso";
            case 45: 
            case 48: return "Nebbia";
            case 51: 
            case 53: 
            case 55: return "Pioviggine";
            case 61: 
            case 63: 
            case 65: return "Pioggia";
            case 71: 
            case 73: 
            case 75: return "Neve";
            case 95: return "Temporale";
            case 96: 
            case 99: return "Temporale con grandine";
            default: return "Non disponibile";
        }
    }
}