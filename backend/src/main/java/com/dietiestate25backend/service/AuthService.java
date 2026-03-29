package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.dto.requests.LoginRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneStaffRequest;
import com.dietiestate25backend.dto.response.LoginResponse;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.error.exception.ConflictException;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.model.Utente;
import com.dietiestate25backend.model.UtenteAgenzia;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {
    private final UtenteAgenziaDao utenteAgenziaDao;
    private final UtenteDao utenteDao;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UtenteDao utenteDao, UtenteAgenziaDao utenteAgenziaDao, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.utenteDao = utenteDao;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.utenteAgenziaDao = utenteAgenziaDao;
    }

    /**
     * Effettua il login e ritorna il JWT
     */
    public LoginResponse login(LoginRequest request) {
        Utente utente;
        try {
            utente = utenteDao.findByEmail(request.getEmail());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException("Email o password non corrette");
        }

        if (!passwordEncoder.matches(request.getPassword(), utente.getPassword())) {
            throw new NotFoundException("Email o password non corrette");
        }

        String token = jwtService.generateToken(utente.getUid(), utente.getRole(), utente.getEmail());

        return new LoginResponse(token, utente.getUid(), utente.getRole());
    }

    /**
     * Registrazione di un Cliente
     */
    public void registraCliente(RegistrazioneRequest request) {
        if (!request.getRole().equals("Cliente")){
            throw new BadRequestException("Solo i clienti possono registrarsi attraverso questo endpoint");
        }
        String uid = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        Utente utente = new Utente(uid, request.getEmail(), hashedPassword, "Cliente");
        try {
            utenteDao.save(utente);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ConflictException("Email già in uso");
        } catch (org.springframework.dao.DataAccessException e) {
            throw new InternalServerErrorException("Errore durante la registrazione del cliente:", e);
        }
    }

    /**
     * Registrazione di Agente o Gestore (solo Admin)
     * Crea l'utente e lo associa a una agenzia via UtenteAgenzia
     */
    @Transactional
    public void registraGestoreOrAgente(String uidAdmin, RegistrazioneStaffRequest request) {
        if  (!request.getRole().equals("Gestore") && !request.getRole().equals("AgenteImmobiliare")){
            throw new BadRequestException("Il ruolo deve essere 'Gestore' o 'AgenteImmobiliare'");
        }
        int idAgenzia;
        try {
            idAgenzia = utenteAgenziaDao.getIdAgenzia(uidAdmin);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new InternalServerErrorException("Errore durante il recupero dell'id agenzia:", e);
        }

        String uid = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        String role = request.getRole();

        Utente utente = new Utente(uid, request.getEmail(), hashedPassword, role);
        try {
            utenteDao.save(utente);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new InternalServerErrorException("Errore durante la registrazione dell'utente:", e);
        }

        // Associa l'utente all'agenzia tramite UtenteAgenzia
        UtenteAgenzia utenteAgenzia = new UtenteAgenzia(idAgenzia, uid);
        try {
            utenteAgenziaDao.save(utenteAgenzia);
        } catch (org.springframework.dao.DataIntegrityViolationException e){
            throw new ConflictException("Utente è gia associato ad un'altra agenzia");
        } catch (org.springframework.dao.DataAccessException e){
            throw new InternalServerErrorException("Errore durante l'associazione dell'utente all'agenzia:", e);
        }
    }

    /**
     * Recupera l'email di un utente dato il suo uid
     */
    public String getEmailByUid(String uid) {
        try {
            return utenteDao.findEmailByUid(uid);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException("Email non trovata per l'utente");
        }
    }

}
