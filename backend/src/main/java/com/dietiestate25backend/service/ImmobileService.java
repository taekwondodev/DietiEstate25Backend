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
    private final ImmobileDao immobileDao;
    private final GeoDataService geoDataService;

    public ImmobileService(ImmobileDao immobileDao, GeoDataService geoDataService) {
        this.immobileDao = immobileDao;
        this.geoDataService = geoDataService;
    }

    public List<Immobile> cercaImmobili(
        String indirizzo,
        Double prezzoMin, Double prezzoMax,
        String nStanze, String tipologia
    ) {
        // Otteniamo le coordinate dall'indirizzo e inseriamole in una Map
        Map<String, Double> coordinate = geoDataService.ottieniCoordinate(indirizzo);

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

    public void creaImmobile(CreaImmobileRequest request, String uidResponsabile) {

        Map<String, Double> coordinate = geoDataService.ottieniCoordinate(request.getIndirizzo());

        // Verifichiamo che le coordinate siano valide
        if (coordinate == null || !coordinate.containsKey("latitudine") || !coordinate.containsKey("longitudine")) {
            throw new BadRequestException("Impossibile ottenere le coordinate per l'indirizzo fornito.");
        }

        Immobile immobile = new Immobile.Builder()
                .setUrlFoto(request.getUrlFoto())
                .setDescrizione(request.getDescrizione())
                .setPrezzo(request.getPrezzo())
                .setDimensione(request.getDimensione())
                .setNBagni(request.getnBagni())
                .setNStanze(request.getnStanze())
                .setTipologia(request.getTipologia())
                .setLatitudine(coordinate.get("latitudine"))
                .setLongitudine(coordinate.get("longitudine"))
                .setIndirizzo(request.getIndirizzo())
                .setPiano(request.getPiano())
                .setHasAscensore(request.isHasAscensore())
                .setHasBalcone(request.isHasBalcone())
                .setIdResponsabile(UUID.fromString(uidResponsabile))
                .build();

        if (!immobileDao.creaImmobile(immobile)) {
            throw new DatabaseErrorException("Impossibile creare l'immobile");
        }
    }
}