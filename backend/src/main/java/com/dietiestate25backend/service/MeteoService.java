package main.java.com.dietiestate25backend.service;

import java.time.LocalDate;

import main.java.com.dietiestate25backend.dto.MeteoResponse;

@Service
public class MeteoService {
    private final MeteoDao meteoDao;

    @Autowired
    public MeteoService(MeteoDao meteoDao) {
        this.meteoDao = meteoDao;
    }

    public boolean isDataNelRange(String date) {
        LocalDate dataRichiesta = LocalDate.parse(date);
        LocalDate dataAttuale = LocalDate.now();
        LocalDate dataMassimaSupportata = dataAttuale.plusDays(7);
        return !dataRichiesta.isAfter(dataMassimaSupportata) && !dataRichiesta.isBefore(dataAttuale);
    }

    public Map<String, Object> ottieniPrevisioni(String city, String date) {
        return meteoDao.ottieniPrevisioni(city, date);
    }

    public void validateToken(String token) {
        TokenUtils.validateToken(token);
    }
}
