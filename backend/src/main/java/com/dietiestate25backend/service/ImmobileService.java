package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.dto.requests.CreaImmobileRequest;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.DatabaseErrorException;
import com.dietiestate25backend.model.Immobile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ImmobileService {
    private static final String LATITUDINE = "latitudine";
    private static final String LONGITUDINE = "longitudine";

    private final ImmobileDao immobileDao;
    private final GeoDataService geoDataService;

    public ImmobileService(ImmobileDao immobileDao, GeoDataService geoDataService) {
        this.immobileDao = immobileDao;
        this.geoDataService = geoDataService;
    }

    public List<Immobile> cercaImmobili(
        String comune, String tipologia,
        Double prezzoMin, Double prezzoMax,
        Double dimensione, Integer nBagni,
        int page, int size
    ) {
        // Creiamo i filters, ovvero le opzioni di ricerca
        Map<String, Object> filters = new HashMap<>();
        filters.put("comune", comune);

        if (tipologia != null) {
            filters.put("tipologia", tipologia);
        }

        if (prezzoMin != null && prezzoMax != null) {
            filters.put("prezzoMin", prezzoMin);
            filters.put("prezzoMax", prezzoMax);
        }

        if (dimensione != null) {
            filters.put("dimensione", dimensione);
        }

        if (nBagni != null) {
            filters.put("nBagni", nBagni);
        }

        return immobileDao.cercaImmobiliConFiltri(filters, page, size);
    }

    public void creaImmobile(CreaImmobileRequest request, String uidResponsabile) {
        Map<String, Double> coordinate = ottieniCoordinate(request.getIndirizzo(), request.getComune());

        Immobile immobile = new Immobile.Builder()
                .setUrlFoto(request.getUrlFoto())
                .setDescrizione(request.getDescrizione())
                .setPrezzo(request.getPrezzo())
                .setDimensione(request.getDimensione())
                .setNBagni(request.getnBagni())
                .setNStanze(request.getnStanze())
                .setTipologia(request.getTipologia())
                .setLatitudine(coordinate.get(LATITUDINE))
                .setLongitudine(coordinate.get(LONGITUDINE))
                .setIndirizzo(request.getIndirizzo())
                .setComune(request.getComune())
                .setPiano(request.getPiano())
                .setHasAscensore(request.isHasAscensore())
                .setHasBalcone(request.isHasBalcone())
                .setIdResponsabile(UUID.fromString(uidResponsabile))
                .build();

        if (!immobileDao.creaImmobile(immobile)) {
            throw new DatabaseErrorException("Impossibile creare l'immobile");
        }
    }

    public List<Immobile> immobiliPersonali(String uidResponsabile) {
        return immobileDao.immobiliPersonali(uidResponsabile);
    }

    private Map<String, Double> ottieniCoordinate(String indirizzo, String comune) {
        // Otteniamo le coordinate dall'indirizzo e inseriamole in una Map
        Map<String, Double> coordinate = geoDataService.ottieniCoordinate(indirizzo, comune);

        // Verifichiamo che le coordinate siano valide
        if (coordinate == null || !coordinate.containsKey(LATITUDINE) || !coordinate.containsKey(LONGITUDINE)) {
            throw new BadRequestException("Impossibile ottenere le coordinate per l'indirizzo fornito.");
        }

        return coordinate;
    }
}