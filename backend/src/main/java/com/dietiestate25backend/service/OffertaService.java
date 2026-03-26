package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.error.exception.DatabaseErrorException;
import com.dietiestate25backend.model.Offerta;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OffertaService {
    private final OffertaDao offertaDao;

    public OffertaService(OffertaDao offertaDao) {
        this.offertaDao = offertaDao;
    }

    public void aggiungiOfferta(Offerta offerta){
        if (!offertaDao.salvaOfferta(offerta)){
            throw new DatabaseErrorException("Offerta non salvata nel database");
        }
    }

    public void aggiornaStatoOfferta(Offerta offerta){
        if (!offertaDao.aggiornaStatoOfferta(offerta)) {
            throw new DatabaseErrorException("Offerta non trovata nel database");
        }
    }

    public List<Offerta> riepilogoOfferteCliente(String idCliente){
        return offertaDao.riepilogoOfferteCliente(idCliente);
    }

    public List<Offerta> riepilogoOfferteUtenteAgenzia(String idAgente){
        return offertaDao.riepilogoOfferteUteneAgenzia(idAgente);
    }

}
