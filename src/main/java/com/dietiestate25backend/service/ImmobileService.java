package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelInterface.ImmobileDao;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Indirizzo;
import com.dietiestate25backend.model.TipoClasseEnergetica;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImmobileService {
    private final ImmobileDao immobileDao;

    public ImmobileService(ImmobileDao immobileDao) {
        this.immobileDao = immobileDao;
    }

    public List<Immobile> cercaImmobili(
            Indirizzo indirizzo, Double prezzoMin, Double prezzoMax,
            String nStanze, String tipologia, TipoClasseEnergetica classeEnergetica
    ) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("cap", indirizzo.getCap());

        if (prezzoMin != null && prezzoMax != null){
            filters.put("prezzoMin", prezzoMin);
            filters.put("prezzoMax", prezzoMax);
        }

        if (nStanze != null){
            filters.put("nStanze", nStanze);
        }

        if(tipologia != null){
            filters.put("tipologia", tipologia);
        }

        if (classeEnergetica != null){
            filters.put("classeEnergetica", classeEnergetica);
        }

        return immobileDao.cercaImmobiliConFiltri(filters);
    }

    public boolean creaImmobile(Immobile immobile) {
        return immobileDao.creaImmobile(immobile);
    }
}
