package main.java.com.dietiestate25backend.service;

import java.time.LocalDate;

import main.java.com.dietiestate25backend.dto.MeteoResponse;

@Service
public class MeteoService {
    private final RestTemplate restTemplate;

    public MeteoService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public boolean isDataNelRange(String date) {
        // Controlliamo se la data inserita è nel range delle previsioni (7 giorni)
        LocalDate dataRichiesta = LocalDate.parse(date);
        LocalDate dataAttuale = LocalDate.now();
        LocalDate dataMassimaSupportata = dataAttuale.plusDays(7);
        return !dataRichiesta.isAfter(dataMassimaSupportata) && !dataRichiesta.isBefore(dataAttuale);
    }

    public MeteoResponse getMeteoPerData(String city, String date) {
        // Costruiamo l'URL per la richiesta API
        String url = String.format(
            "https://api.open-meteo.com/v1/forecast?city=%s&date=%s&hourly=temperature_2m",
            city, date
        );

        // RestTemplate mappa automaticamente il risultato della chiamata API in MeteoResponse
        return restTemplate.getForObject(url, MeteoResponse.class);
    }
}
