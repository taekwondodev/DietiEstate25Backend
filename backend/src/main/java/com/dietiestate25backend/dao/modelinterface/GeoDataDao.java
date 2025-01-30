package com.dietiestate25backend.dao.modelinterface;

import java.util.List;
import java.util.Map;

public interface GeoDataDao {
    Map<String, Integer> ottieniConteggioPuntiInteresse(double latitudine, double longitudine, int raggio, List<String> categorie);
    Map<String, Double> ottieniCoordinate(String indirizzoCompleto);
}