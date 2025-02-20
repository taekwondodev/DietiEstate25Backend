package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.GeoDataDao;
import com.dietiestate25backend.dto.requests.ConteggioPuntiInteresseRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GeoDataService {
    private final GeoDataDao geoDataDao;

    public GeoDataService(GeoDataDao geoDataDao) {
        this.geoDataDao = geoDataDao;
    }

    public Map<String, Integer> ottieniConteggioPuntiInteresse(ConteggioPuntiInteresseRequest conteggioPuntiInteresseRequest) {
        return geoDataDao.ottieniConteggioPuntiInteresse(
            conteggioPuntiInteresseRequest.getLatitudine(),
            conteggioPuntiInteresseRequest.getLongitudine(),
            conteggioPuntiInteresseRequest.getRaggio(),
            conteggioPuntiInteresseRequest.getCategorie()
        );
    }

    public Map<String, Double> ottieniCoordinate(String indirizzo, String comune) {
        String indirizzoCompleto = indirizzo + ", " + comune;
        return geoDataDao.ottieniCoordinate(indirizzoCompleto);
    }
}