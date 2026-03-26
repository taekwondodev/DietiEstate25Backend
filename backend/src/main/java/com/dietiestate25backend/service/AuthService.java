package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.postgresimplements.AdminPostgres;
import com.dietiestate25backend.dao.postgresimplements.UtenteAgenziaPostgres;
import com.dietiestate25backend.dao.postgresimplements.UtentePostgres;
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
    private final AdminPostgres adminPostgres;
    private final UtenteAgenziaPostgres utenteAgenziaPostgres;
    private final UtentePostgres utentePostgres;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AdminPostgres adminPostgres, UtenteAgenziaPostgres utenteAgenziaPostgres, UtentePostgres utentePostgres, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.utentePostgres = utentePostgres;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.adminPostgres = adminPostgres;
        this.utenteAgenziaPostgres = utenteAgenziaPostgres;
    }

    /**
     * Effettua il login e ritorna il JWT
     */
    public LoginResponse login(LoginRequest request) {
        Utente utente = utentePostgres.findByEmail(request.getEmail());

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
        utentePostgres.save(utente);
    }

    /**
     * Registrazione di Agente o Gestore (solo Admin)
     * Crea l'utente e lo associa a una agenzia via UtenteAgenzia
     */
    public void registraGestoreOrAgente(String uidAdmin, RegistrazioneRequest request) {
        int idAgenzia = adminPostgres.getIdAgenzia(uidAdmin);

        String uid = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        String role = request.getRole();

        Utente utente = new Utente(uid, request.getEmail(), hashedPassword, role);
        utentePostgres.save(utente);

        // Associa l'utente all'agenzia tramite UtenteAgenzia
        UtenteAgenzia utenteAgenzia = new UtenteAgenzia(idAgenzia, uid);
        utenteAgenziaPostgres.save(utenteAgenzia);
    }

    /**
     * Recupera l'email di un utente dato il suo uid
     */
    public String getEmailByUid(String uid) {
        try {
            return utentePostgres.findEmailByUid(uid);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException("Email non trovata per l'utente");
        }
    }

}
