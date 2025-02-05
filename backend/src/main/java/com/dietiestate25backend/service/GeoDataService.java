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

    public Map<String, Double> ottieniCoordinate(String indirizzo, String numeroCivico, String città) {
        // Concateniamo indirizzo, numero civico e città per ottenere un input più preciso
        String indirizzoCompleto = indirizzo + " " + numeroCivico;
        if (città != null && !città.trim().isEmpty()) {
            indirizzoCompleto += ", " + città;
        }
        
        return geoDataDao.ottieniCoordinate(indirizzoCompleto);
    }
}