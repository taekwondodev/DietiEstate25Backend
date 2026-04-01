package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.dto.requests.AggiornaOffertaRequest;
import com.dietiestate25backend.dto.requests.CreaOffertaRequest;
import com.dietiestate25backend.error.ErrorCode;
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
            throw new BadRequestException(ErrorCode.INVALID_CLIENT_ID);
        }
        try {
            if (!offertaDao.salvaOfferta(request.getImporto(), StatoOfferta.IN_SOSPESO, uidCliente, request.getIdImmobile())) {
                throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR);
            }
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("idimmobile")){
                throw new NotFoundException(ErrorCode.IMMOBILE_NOT_FOUND);
            }
            if (e.getMessage() != null && e.getMessage().contains("idcliente")) {
                throw new NotFoundException(ErrorCode.CLIENT_NOT_FOUND);
            }
        } catch (InternalServerErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    public void aggiornaStatoOfferta(AggiornaOffertaRequest request, String uidUtente){
        if (uidUtente == null || uidUtente.trim().isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_USER_ID);
        }
        StatoOfferta nuovoStato;
        try {
            nuovoStato = StatoOfferta.fromString(request.getStato());
        }  catch (IllegalArgumentException e) {
            throw new BadRequestException(ErrorCode.INVALID_STATUS);
        }
        try {
            Offerta offertaAttuale = offertaDao.getOffertaById(request.getIdOfferta());

            if (offertaAttuale == null) {
                throw new NotFoundException(ErrorCode.OFFERTA_NOT_FOUND);
            }

            String ruoloUtente = TokenUtils.getRole();

            if (ruoloUtente.equals("Cliente")) {
                if (!offertaAttuale.getIdCliente().equals(uidUtente)) {
                    throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
                }
            } else {
                if (!offertaAttuale.getImmobile().getIdResponsabile().equals(uidUtente)) {
                    throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
                }
            }

            StatoOfferta statoAttuale = offertaAttuale.getStato();
            if (!isTransazioneValida(statoAttuale, nuovoStato)) {
                throw new BadRequestException(ErrorCode.INVALID_OFFERTA_STATUS);
            }

            Offerta offerta = new Offerta(request.getIdOfferta(), offertaAttuale.getImporto(), nuovoStato, offertaAttuale.getIdCliente(), offertaAttuale.getImmobile());
            if (!offertaDao.aggiornaStatoOfferta(offerta)) {
                throw new DatabaseErrorException(ErrorCode.DATABASE_READ_ERROR);
            }
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException(ErrorCode.OFFERTA_NOT_FOUND);
        }
        catch(BadRequestException | NotFoundException | DatabaseErrorException | UnauthorizedException e){
            throw e;
        }
        catch (Exception e) {
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    public List<Offerta> riepilogoOfferteCliente(String idCliente){
        if (idCliente == null || idCliente.trim().isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_CLIENT_ID);
        }

        return offertaDao.riepilogoOfferteCliente(idCliente);
    }

    public List<Offerta> riepilogoOfferteUtenteAgenzia(String idAgente){
        if (idAgente == null || idAgente.trim().isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_USER_ID);
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