package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.error.exception.DatabaseErrorException;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Visita;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VisitaService {
    private final VisitaDao visitaDao;
    private final EmailService emailService;
    private final AuthService authService;

    public VisitaService(VisitaDao visitaDao, EmailService emailService, AuthService authService) {
        this.visitaDao = visitaDao;
        this.emailService = emailService;
        this.authService = authService;
    }

    public void prenotaVisita(Visita visita) {
        if (!visitaDao.salva(visita)) {
            throw new DatabaseErrorException("Visita non salvata nel database");
        }

        inviaEmail(visita.getImmobile());
    }

    public void aggiornaStatoVisita(Visita visita) {
        if (!visitaDao.aggiornaStato(visita)) {
            throw new DatabaseErrorException("Visita non trovata nel database");
        }
    }

    public List<Visita> riepilogoVisiteCliente(UUID idCliente) {
        return visitaDao.riepilogoVisiteCliente(idCliente);
    }

    public List<Visita> riepilogoVisiteUtenteAgenzia(UUID idAgente) {
        return visitaDao.riepilogoVisiteUtenteAgenzia(idAgente);
    }

    private void inviaEmail(Immobile immobile){
        String idResponsabile = immobile.getIdResponsabile().toString();
        String agenteEmail = authService.getEmailByUid(idResponsabile);

        String oggetto = "Nuova prenotazione visita";
        String testo = "È stata prenotata una nuova visita per l'immobile: " + immobile.getDescrizione();
        emailService.inviaEmail(agenteEmail, oggetto, testo);
    }
}
