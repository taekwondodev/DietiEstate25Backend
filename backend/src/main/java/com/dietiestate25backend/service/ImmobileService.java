package com.dietiestate25backend.service;

import com.dietiestate25backend.utils.TokenUtils;
import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.TipoClasseEnergetica;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImmobileService {
    private final ImmobileDao immobileDao;
    private final GeoDataService geoDataService;

    public ImmobileService(ImmobileDao immobileDao, GeoDataService geoDataService) {
        this.immobileDao = immobileDao;
        this.geoDataService = geoDataService;
    }

    public List<Immobile> cercaImmobili(
        String indirizzo, String numeroCivico, String città,
        Double prezzoMin, Double prezzoMax,
        String nStanze, String tipologia, TipoClasseEnergetica classeEnergetica
    ) {
        // Otteniamo le coordinate dall'indirizzo e inseriamole in una Map
        Map<String, Double> coordinate = geoDataService.ottieniCoordinate(indirizzo, numeroCivico, città);

        if (coordinate == null || !coordinate.containsKey("latitudine") || !coordinate.containsKey("longitudine")) {
            throw new BadRequestException("Impossibile ottenere le coordinate per l'indirizzo fornito.");
        }

        // Creiamo i filters, ovvero le opzioni di ricerca (nei filtri sono inclusi anche le coordinate, di base)
        Map<String, Object> filters = new HashMap<>();
        filters.put("latitudine", coordinate.get("latitudine"));
        filters.put("longitudine", coordinate.get("longitudine"));

        if (prezzoMin != null && prezzoMax != null) {
            filters.put("prezzoMin", prezzoMin);
            filters.put("prezzoMax", prezzoMax);
        }

        if (nStanze != null) {
            filters.put("nStanze", nStanze);
        }

        if (tipologia != null) {
            filters.put("tipologia", tipologia);
        }

        if (classeEnergetica != null) {
            filters.put("classeEnergetica", classeEnergetica);
        }

        return immobileDao.cercaImmobiliConFiltri(filters);
    }

    public void creaImmobile(Immobile immobile) {

        Map<String, Double> coordinate = geoDataService.ottieniCoordinate(
            immobile.getIndirizzo(), immobile.getNumeroCivico(), immobile.getCittà()
        );

        // Verifichiamo che le coordinate siano valide
        if (coordinate == null || !coordinate.containsKey("latitudine") || !coordinate.containsKey("longitudine")) {
            throw new BadRequestException("Impossibile ottenere le coordinate per l'indirizzo fornito.");
        }

        immobile.setLatitudine(coordinate.get("latitudine"));
        immobile.setLongitudine(coordinate.get("longitudine"));

        if (!immobileDao.creaImmobile(immobile)) {
            throw new BadRequestException("Errore durante la creazione dell'immobile.");
        }
    }
}
}
