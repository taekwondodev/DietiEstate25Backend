package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.dto.requests.LoginRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.dto.response.LoginResponse;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.model.Utente;
import com.dietiestate25backend.model.UtenteAgenzia;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        Utente utente = utenteDao.findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), utente.getPassword())) {
            throw new UnauthorizedException("Email o password non corrette");
        }

        String token = jwtService.generateToken(utente.getUid(), utente.getRole(), utente.getEmail());

        return new LoginResponse(token, utente.getUid(), utente.getRole());
    }

    /**
     * Registrazione di un Cliente
     */
    public void registraCliente(RegistrazioneRequest request) {
        String uid = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        Utente utente = new Utente(uid, request.getEmail(), hashedPassword, "Cliente");
        utenteDao.save(utente);
    }

    /**
     * Registrazione di Agente o Gestore (solo Admin)
     * Crea l'utente e lo associa a una agenzia via UtenteAgenzia
     */
    public void registraGestoreOrAgente(String uidAdmin, RegistrazioneRequest request) {
        int idAgenzia = utenteAgenziaDao.getIdAgenzia(uidAdmin);

        String uid = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        String role = request.getRole();

        Utente utente = new Utente(uid, request.getEmail(), hashedPassword, role);
        utenteDao.save(utente);

        // Associa l'utente all'agenzia tramite UtenteAgenzia
        UtenteAgenzia utenteAgenzia = new UtenteAgenzia(idAgenzia, uid);
        utenteAgenziaDao.save(utenteAgenzia);
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
