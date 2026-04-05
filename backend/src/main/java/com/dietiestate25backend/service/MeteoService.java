package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.MeteoDao;
import com.dietiestate25backend.error.ErrorCode;
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
            throw new BadRequestException(ErrorCode.INVALID_DATE_FORMAT);
        }

        LocalDate dataAttuale = LocalDate.now();
        LocalDate dataMassimaSupportata = dataAttuale.plusDays(7);

        if (dataRichiesta.isBefore(dataAttuale) || dataRichiesta.isAfter(dataMassimaSupportata)) {
            throw new BadRequestException(ErrorCode.INVALID_DATE_RANGE);
        }

        double latitudineDouble;
        double longitudineDouble;

        try {
            latitudineDouble = Double.parseDouble(latitudine);
            longitudineDouble = Double.parseDouble(longitudine);
        } catch (NumberFormatException e) {
            throw new BadRequestException(ErrorCode.INVALID_COORDINATES);
        }

        if (latitudineDouble < -90 || latitudineDouble > 90) {
            throw new BadRequestException(ErrorCode.INVALID_LATITUDE);
        }
        if (longitudineDouble < -180 || longitudineDouble > 180) {
            throw new BadRequestException(ErrorCode.INVALID_LONGITUDE);
        }

        return meteoDao.ottieniPrevisioni(latitudineDouble, longitudineDouble, date);
    }

}