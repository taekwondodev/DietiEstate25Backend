package com.dietiestate25backend.service;

import com.dietiestate25backend.config.TokenUtils;
import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.dao.postgresimplements.ImmobilePostgres;
import com.dietiestate25backend.dao.postgresimplements.OffertaPostgres;
import com.dietiestate25backend.dto.OffertaRequest;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Offerta;
import org.springframework.stereotype.Service;

@Service
public class OffertaService {
    private final OffertaDao offertaDao;
    private final ImmobileDao immobileDao;

    public OffertaService(OffertaPostgres offertaDao, ImmobilePostgres immobileDao) {
        this.offertaDao = offertaDao;
        this.immobileDao = immobileDao;
    }

    public boolean aggiungiOfferta(OffertaRequest request){
        Immobile immobile = request.getImmobile();
        int idImmobile = immobileDao.getIdImmobile(immobile);

        Offerta offerta = new Offerta(request.getImporto(), request.getStato(), request.getIdCliente(), idImmobile);

        return offertaDao.salvaOfferta(offerta);
    }

    public String getUidFromToken(String token) {
        return TokenUtils.getUidFromToken(token);
    }
}
