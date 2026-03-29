package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.dto.requests.CreaImmobileRequest;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.ConflictException;
import com.dietiestate25backend.error.exception.DatabaseErrorException;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.model.Immobile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // Limite massimo per evitare buffer overflow
        if (size > 100) {
            throw new BadRequestException("Size non può superare 100");
        }

        // Validazione: prezzoMin e prezzoMax coerenti
        if (prezzoMin != null && prezzoMax != null) {
            if (prezzoMin < 0 || prezzoMax < 0) {
                throw new BadRequestException("Prezzi non possono essere negativi");
            }
            if (prezzoMin > prezzoMax) {
                throw new BadRequestException("PrezzoMin non può essere maggiore di PrezzoMax");
            }
        }

        // Validazione: dimensione non negativa
        if (dimensione != null && dimensione < 0) {
            throw new BadRequestException("Dimensione non può essere negativa");
        }

        // Validazione: nBagni non negativo
        if (nBagni != null && nBagni < 0) {
            throw new BadRequestException("nBagni non può essere negativo");
        }

        // Creiamo i filters
        Map<String, Object> filters = new HashMap<>();
        filters.put("comune", comune.trim());

        if (tipologia != null && !tipologia.trim().isEmpty()) {
            filters.put("tipologia", tipologia.trim());
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

        try {
            return immobileDao.cercaImmobiliConFiltri(filters, page, size);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new InternalServerErrorException("Errore durante la ricerca", e);
        }
    }


    public void creaImmobile(CreaImmobileRequest request, String uidResponsabile) {
        if (uidResponsabile == null || uidResponsabile.trim().isEmpty()) {
            throw new BadRequestException("Responsabile non valido");
        }
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
                .setIdResponsabile(uidResponsabile)
                .build();

        try {
            if (!immobileDao.creaImmobile(immobile)) {
                throw new DatabaseErrorException("Impossibile creare l'immobile");
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ConflictException("Attributi di immobile non validi");
        } catch (org.springframework.dao.DataAccessException e) {
            throw new InternalServerErrorException("Errore durante la creazione dell'immobile", e);
        }
    }

    public List<Immobile> immobiliPersonali(String uidResponsabile) {
        if (uidResponsabile == null || uidResponsabile.trim().isEmpty()) {
            throw new BadRequestException("Responsabile non valido");
        }
        try {
            return immobileDao.immobiliPersonali(uidResponsabile);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new InternalServerErrorException("Errore durante il recupero degli immobili personali", e);
        }
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