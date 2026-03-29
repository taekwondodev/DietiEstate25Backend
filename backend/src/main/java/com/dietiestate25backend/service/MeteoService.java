package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.MeteoDao;
import com.dietiestate25backend.error.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Service
public class MeteoService {
    private final MeteoDao meteoDao;

    public MeteoService(MeteoDao meteoDao) {
        this.meteoDao = meteoDao;
    }

    public Map<String, Object> ottieniPrevisioni(String latitudine, String longitudine, String date) {
        LocalDate dataRichiesta;
        try {
            dataRichiesta = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("La data fornita non è in un formato valido. Utilizzare il formato YYYY-MM-DD.");
        }

        LocalDate dataAttuale = LocalDate.now();
        LocalDate dataMassimaSupportata = dataAttuale.plusDays(7);

        if (dataRichiesta.isBefore(dataMassimaSupportata)) {
            throw new BadRequestException("Le previsioni meteo sono disponibili solo da oggi ai prossimi 7 giorni");
        }

        double latitudineDouble;
        double longitudineDouble;

        try {
            latitudineDouble = Double.parseDouble(latitudine);
            longitudineDouble = Double.parseDouble(longitudine);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Latitudine o longitudine non valide: devono essere numeri.");
        }

        if (latitudineDouble < -90 || latitudineDouble > 90) {
            throw new BadRequestException("Latitudine deve essere tra -90 e 90.");
        }
        if (longitudineDouble < -180 || longitudineDouble > 180) {
            throw new BadRequestException("Longitudine deve essere tra -180 e 180.");
        }

        return meteoDao.ottieniPrevisioni(latitudineDouble, longitudineDouble, date);
    }

}
