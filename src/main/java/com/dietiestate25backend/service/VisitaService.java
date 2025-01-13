package com.dietiestate25backend.service;

import com.dietiestate25backend.config.TokenUtils;
import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.dto.VisitaRequest;
import com.dietiestate25backend.error.exception.DatabaseErrorException;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Visita;
import org.springframework.stereotype.Service;

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

    public void prenotaVisita(VisitaRequest request) {
        Immobile immobile = request.getImmobile();
        int idImmobile = immobileDao.getIdImmobile(immobile);
        String idResponsabile = immobile.getIdResponsabile().toString();

        Visita visita = new Visita(
                request.getDataRichiesta(), request.getDataVisita(), request.getOraVisita(),
                request.getStato(), request.getIdCliente(), idImmobile
        );

        if (!visitaDao.salva(visita)) {
            throw new DatabaseErrorException("Visita non salvata nel database");
        }

        String agenteEmail = authService.getEmailByUid(idResponsabile);
        String oggetto = "Nuova prenotazione visita";
        String testo = "È stata prenotata una nuova visita per l'immobile: " + immobile.getDescrizione();
        emailService.inviaEmail(agenteEmail, oggetto, testo);
    }

    public void aggiornaStatoVisita(VisitaRequest request) {
        Immobile immobile = request.getImmobile();
        int idImmobile = immobileDao.getIdImmobile(immobile);

        Visita visita = new Visita(
                request.getDataRichiesta(), request.getDataVisita(), request.getOraVisita(),
                request.getStato(), request.getIdCliente(), idImmobile
        );

        if (!visitaDao.aggiornaStato(visita)) {
            throw new DatabaseErrorException("Visita non trovata nel database");
        }
    }

    public String getUidFromToken(String token) {
        return TokenUtils.getUidFromToken(token);
    }
}
