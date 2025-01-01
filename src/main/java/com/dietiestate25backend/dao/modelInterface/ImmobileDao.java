package com.dietiestate25backend.dao.modelInterface;

import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Indirizzo;
import com.dietiestate25backend.model.TipoClasseEnergetica;

import java.util.List;
import java.util.Map;

public interface ImmobileDao {
    List<Immobile> cercaImmobiliConFiltri(Map<String, Object> filters);
}
