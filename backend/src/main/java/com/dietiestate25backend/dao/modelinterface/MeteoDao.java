package com.dietiestate25backend.dao.modelinterface;

import java.util.Map;

public interface MeteoDao {
    Map<String, Object> ottieniPrevisioni(double latitudine, double longitudine, String data);
}