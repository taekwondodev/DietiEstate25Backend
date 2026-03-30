package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.dto.requests.AggiornaOffertaRequest;
import com.dietiestate25backend.dto.requests.CreaOffertaRequest;
import com.dietiestate25backend.error.exception.*;
import com.dietiestate25backend.model.Offerta;
import com.dietiestate25backend.model.StatoOfferta;
import com.dietiestate25backend.utils.TokenUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OffertaService {
    private final OffertaDao offertaDao;

    public OffertaService(OffertaDao offertaDao) {
        this.offertaDao = offertaDao;
    }

    public void aggiungiOfferta(CreaOffertaRequest request, String uidCliente){
        if (uidCliente == null || uidCliente.trim().isEmpty()) {
            throw new BadRequestException("Uid cliente non valido");
        }
        try {
            if (!offertaDao.salvaOfferta(request.getImporto(), StatoOfferta.IN_SOSPESO, uidCliente, request.getIdImmobile())) {
                throw new InternalServerErrorException("Offerta non salvata nel database");
            }
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("idimmobile")){
                throw new NotFoundException("Immobile non trovato");
            }
            if (e.getMessage() != null && e.getMessage().contains("idcliente")) {
                throw new NotFoundException("Cliente non trovato");
            }
        } catch (InternalServerErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException("Errore interno del server");
        }
    }

    public void aggiornaStatoOfferta(AggiornaOffertaRequest request, String uidUtente){
        if (uidUtente == null || uidUtente.trim().isEmpty()) {
            throw new BadRequestException("Uid utente non valido");
        }
        StatoOfferta nuovoStato;
        try {
            nuovoStato = StatoOfferta.fromString(request.getStato());
        }  catch (IllegalArgumentException e) {
            throw new BadRequestException("Stato non valido");
        }
        try {
            Offerta offertaAttuale = offertaDao.getOffertaById(request.getIdOfferta());

            if (offertaAttuale == null) {
                throw new NotFoundException("Offerta non trovato");
            }

            String ruoloUtente = TokenUtils.getRole();

            if (ruoloUtente.equals("Cliente")) {
                if (!offertaAttuale.getIdCliente().equals(uidUtente)) {
                    throw new UnauthorizedException("Utente non autorizzato");
                }
            } else {
                if (!offertaAttuale.getImmobile().getIdResponsabile().equals(uidUtente)) {
                    throw new UnauthorizedException("Utente non autorizzato");
                }
            }

            StatoOfferta statoAttuale = offertaAttuale.getStato();
            if (!isTransazioneValida(statoAttuale, nuovoStato)) {
                throw new BadRequestException("Stato Offerta non valido");
            }

            Offerta offerta = new Offerta(request.getIdOfferta(), offertaAttuale.getImporto(), nuovoStato, offertaAttuale.getIdCliente(), offertaAttuale.getImmobile());
            if (!offertaDao.aggiornaStatoOfferta(offerta)) {
                throw new DatabaseErrorException("Offerta non trovata nel database");
            }
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException("Offerta non trovata");
        }
        catch(BadRequestException | NotFoundException | DatabaseErrorException | UnauthorizedException e){
            throw e;
        }
        catch (Exception e) {
            throw new InternalServerErrorException("Errore interno del server");
        }
    }

    public List<Offerta> riepilogoOfferteCliente(String idCliente){
        if (idCliente == null || idCliente.trim().isEmpty()) {
            throw new BadRequestException("Uid cliente non valido");
        }

        return offertaDao.riepilogoOfferteCliente(idCliente);
    }

    public List<Offerta> riepilogoOfferteUtenteAgenzia(String idAgente){
        if (idAgente == null || idAgente.trim().isEmpty()) {
            throw new BadRequestException("Uid utente non valido");
        }

        return offertaDao.riepilogoOfferteUteneAgenzia(idAgente);
    }

    private boolean isTransazioneValida(StatoOfferta statoAttuale, StatoOfferta nuovoStato) {
        if ( statoAttuale == nuovoStato ){ return false; }

        switch (statoAttuale) {
            case IN_SOSPESO:
                return nuovoStato == StatoOfferta.ACCETTATA || nuovoStato == StatoOfferta.RIFIUTATA;
            case ACCETTATA:
            default:
                return false;
        }
    }
}
