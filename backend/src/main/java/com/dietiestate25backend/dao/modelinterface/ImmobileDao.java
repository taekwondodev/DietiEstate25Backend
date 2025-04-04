package com.dietiestate25backend.dao.modelinterface;

import com.dietiestate25backend.model.Immobile;

import java.util.List;
import java.util.Map;

public interface ImmobileDao {
    List<Immobile> cercaImmobiliConFiltri(Map<String, Object> filters, int page, int size);
    boolean creaImmobile(Immobile immobile);
    List<Immobile> immobiliPersonali(String uidResponsabile);
}
