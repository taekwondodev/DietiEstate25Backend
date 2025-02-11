package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.GeoDataDao;
import com.dietiestate25backend.dao.modelinterface.MeteoDao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class MeteoService {
    private final GeoDataDao geoDataDao;
    private final MeteoDao meteoDao;

    public MeteoService(GeoDataDao geoDataDao, MeteoDao meteoDao) {
        this.geoDataDao = geoDataDao;
        this.meteoDao = meteoDao;
    }

    public boolean isDataNelRange(String date) {
        LocalDate dataRichiesta = LocalDate.parse(date);
        LocalDate dataAttuale = LocalDate.now();
        LocalDate dataMassimaSupportata = dataAttuale.plusDays(7);
        return !dataRichiesta.isAfter(dataMassimaSupportata) && !dataRichiesta.isBefore(dataAttuale);
    }

    public Map<String, Object> ottieniPrevisioni(String latitudine, String longitudine, String date) {

        // Convertiamo i valori delle Stringhe di latitudine e longitudine Double all'interno di una Map
        Map<String, Double> coordinate = new HashMap<>();

        try {
            coordinate.put("latitudine", Double.parseDouble(latitudine));
            coordinate.put("longitudine", Double.parseDouble(longitudine));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Latitudine o longitudine non valide: devono essere numeri.", e);
        }

        return meteoDao.ottieniPrevisioni(coordinate.get("latitudine"), coordinate.get("longitudine"), date);

    }

}
