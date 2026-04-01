package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.dto.requests.AggiornaVisitaRequest;
import com.dietiestate25backend.dto.requests.PrenotaVisitaRequest;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.*;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.StatoVisita;
import com.dietiestate25backend.model.Visita;
import com.dietiestate25backend.utils.TokenUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

@Service
public class VisitaService {
    private final VisitaDao visitaDao;
    private final ImmobileDao immobileDao;
    private final EmailService emailService;
    private final AuthService authService;

    public VisitaService(VisitaDao visitaDao, ImmobileDao immobileDao, EmailService emailService, AuthService authService) {
        this.visitaDao = visitaDao;
        this.immobileDao = immobileDao;
        this.emailService = emailService;
        this.authService = authService;
    }

    public void prenotaVisita(PrenotaVisitaRequest request, String uidCliente) {
        if (uidCliente == null || uidCliente.trim().isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_CLIENT_ID);
        }

        LocalTime oraInizio = LocalTime.of(8, 0);
        LocalTime oraFine = LocalTime.of(18, 0);
        if (request.getOraVisita().isBefore(oraInizio) || request.getOraVisita().isAfter(oraFine)) {
            throw new BadRequestException(ErrorCode.INVALID_VISIT_TIME);
        }
        try {
            java.sql.Date sqlDate = java.sql.Date.valueOf(request.getDataVisita());
            java.sql.Time sqlTime = Time.valueOf(request.getOraVisita());

            if (!visitaDao.salva(sqlDate, sqlTime, StatoVisita.IN_SOSPESO, uidCliente, request.getIdImmobile())) {
                throw new DatabaseErrorException(ErrorCode.DATABASE_WRITE_ERROR);
            }

            try {
                inviaEmail(request.getIdImmobile());
            } catch (Exception e) {
                System.err.println("Errore durante l'invio dell'email: " + e.getMessage());
            }
        } catch(DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("immobile")) {
                throw new NotFoundException(ErrorCode.IMMOBILE_NOT_FOUND);
            }
            if (e.getMessage() != null && e.getMessage().contains("idcliente")) {
                throw new NotFoundException(ErrorCode.CLIENT_NOT_FOUND);
            }
        } catch (BadRequestException | NotFoundException | ConflictException | DatabaseErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    public void aggiornaStatoVisita(AggiornaVisitaRequest request, String uidUtente) {
        if (uidUtente == null || uidUtente.trim().isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_USER_ID);
        }
        StatoVisita nuovoStato;
        try {
            nuovoStato = StatoVisita.fromString(request.getStato());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(ErrorCode.INVALID_STATUS);
        }

        try {
            Visita visitaAttuale = visitaDao.getVisitaById(request.getIdVisita());
            if (visitaAttuale == null) {
                throw new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND);
            }

            String ruoloUtente = TokenUtils.getRole();

            if(ruoloUtente.equals("Cliente")) {
                if(!visitaAttuale.getIdCliente().equals(uidUtente)) {
                    throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
                }
            } else {
                if(!visitaAttuale.getImmobile().getIdResponsabile().equals(uidUtente)) {
                    throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
                }
            }

            StatoVisita statoAttuale = visitaAttuale.getStato();
            if (!isTransizioneValidaVisita(statoAttuale, nuovoStato)) {
                throw new BadRequestException(ErrorCode.INVALID_STATUS);
            }

            Visita visita = new Visita(
                    request.getIdVisita(), visitaAttuale.getDataVisita(),
                    visitaAttuale.getOraVisita(), nuovoStato,
                    visitaAttuale.getIdCliente(), visitaAttuale.getImmobile()
            );
            if (!visitaDao.aggiornaStato(visita)) {
                throw new DatabaseErrorException(ErrorCode.DATABASE_READ_ERROR);
            }
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        catch (BadRequestException | NotFoundException | UnauthorizedException | DatabaseErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    public List<Visita> riepilogoVisiteCliente(String idCliente) {
        if  (idCliente == null || idCliente.trim().isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_CLIENT_ID);
        }
        return visitaDao.riepilogoVisiteCliente(idCliente);
    }

    public List<Visita> riepilogoVisiteUtenteAgenzia(String idAgente) {
        if (idAgente == null || idAgente.trim().isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_USER_ID);
        }
        return visitaDao.riepilogoVisiteUtenteAgenzia(idAgente);
    }

    private void inviaEmail(int idImmobile) {
        Immobile immobile = immobileDao.getImmobileById(idImmobile);
        String idResponsabile = immobile.getIdResponsabile();
        String agenteEmail = authService.getEmailByUid(idResponsabile);

        String oggetto = "Nuova prenotazione visita";
        String testo = "È stata prenotata una nuova visita per l'immobile: " + immobile.getDescrizione();
        emailService.inviaEmail(agenteEmail, oggetto, testo);
    }

    private boolean isTransizioneValidaVisita(StatoVisita statoAttuale, StatoVisita nuovoStato){
        if (statoAttuale == nuovoStato) {
            return false;
        }

        switch (statoAttuale) {
            case IN_SOSPESO:
                return nuovoStato == StatoVisita.CONFERMATA || nuovoStato == StatoVisita.RIFIUTATA;
            case CONFERMATA:
            default:
                return false;
        }
    }
}