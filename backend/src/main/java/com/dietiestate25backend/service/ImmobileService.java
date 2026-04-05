package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.dto.requests.CreaImmobileRequest;
import com.dietiestate25backend.error.ErrorCode;
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
        if (size > 100) {
            throw new BadRequestException(ErrorCode.INVALID_PAGE_SIZE);
        }

        if (prezzoMin != null && prezzoMax != null) {
            if (prezzoMin < 0 || prezzoMax < 0) {
                throw new BadRequestException(ErrorCode.INVALID_PRICE);
            }
            if (prezzoMin > prezzoMax) {
                throw new BadRequestException(ErrorCode.INVALID_PRICE_RANGE);
            }
        }

        if (dimensione != null && dimensione < 0) {
            throw new BadRequestException(ErrorCode.INVALID_DIMENSION);
        }

        if (nBagni != null && nBagni < 0) {
            throw new BadRequestException(ErrorCode.INVALID_BATHROOMS);
        }

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
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }
    }


    public void creaImmobile(CreaImmobileRequest request, String uidResponsabile) {
        if (uidResponsabile == null || uidResponsabile.trim().isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_RESPONSABILE);
        }
        Map<String, Double> coordinate = ottieniCoordinate(request.getIndirizzo(), request.getComune());

        Immobile immobile = Immobile.builder()
                .urlFoto(request.getUrlFoto())
                .descrizione(request.getDescrizione())
                .prezzo(request.getPrezzo())
                .dimensione(request.getDimensione())
                .nBagni(request.getNBagni())
                .nStanze(request.getNStanze())
                .tipologia(request.getTipologia())
                .latitudine(coordinate.get(LATITUDINE))
                .longitudine(coordinate.get(LONGITUDINE))
                .indirizzo(request.getIndirizzo())
                .comune(request.getComune())
                .piano(request.getPiano())
                .hasAscensore(request.isHasAscensore())
                .hasBalcone(request.isHasBalcone())
                .idResponsabile(uidResponsabile)
                .build();

        try {
            if (!immobileDao.creaImmobile(immobile)) {
                throw new DatabaseErrorException(ErrorCode.DATABASE_WRITE_ERROR);
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ConflictException(ErrorCode.INVALID_IMMOBILE_ATTRIBUTES);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    public List<Immobile> immobiliPersonali(String uidResponsabile) {
        if (uidResponsabile == null || uidResponsabile.trim().isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_RESPONSABILE);
        }
        try {
            return immobileDao.immobiliPersonali(uidResponsabile);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    private Map<String, Double> ottieniCoordinate(String indirizzo, String comune) {
        Map<String, Double> coordinate = geoDataService.ottieniCoordinate(indirizzo, comune);

        if (coordinate == null || !coordinate.containsKey(LATITUDINE) || !coordinate.containsKey(LONGITUDINE)) {
            throw new BadRequestException(ErrorCode.INVALID_ADDRESS);
        }

        return coordinate;
    }
}
