package main.java.com.dietiestate25backend.service;

@Service
public class MeteoService {
    private final RestTemplate restTemplate;

    public MeteoService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public boolean isDataNelRange(String date) {
        // Controlla se dataRichiesta è nel range di dataMassimaSupportata delle previsioni (7 giorni)
        LocalDate dataRichiesta = LocalDate.parse(date);
        LocalDate dataAttuale = LocalDate.now();
        LocalDate dataMassimaSupportata = dataAttuale.plusDays(7);
        return !dataRichiesta.isAfter(ataMassimaSupportata) && !dataRichiesta.isBefore(dataAttuale);
    }

    public MeteoResponse getMeteoPerData(String city, String date) {
        String url = String.format(
            "https://api.open-meteo.com/v1/forecast?city=%s&date=%s",
            city, date
        );
        return restTemplate.getForObject(url, MeteoResponse.class);
    }
}
