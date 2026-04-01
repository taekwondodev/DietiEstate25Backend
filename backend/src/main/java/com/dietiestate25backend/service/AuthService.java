package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.dto.requests.LoginRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.dto.requests.RegistrazioneStaffRequest;
import com.dietiestate25backend.dto.response.LoginResponse;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.*;
import com.dietiestate25backend.model.Utente;
import com.dietiestate25backend.model.UtenteAgenzia;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {
    private final UtenteAgenziaDao utenteAgenziaDao;
    private final UtenteDao utenteDao;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(UtenteDao utenteDao, UtenteAgenziaDao utenteAgenziaDao, JwtService jwtService, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.utenteDao = utenteDao;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.utenteAgenziaDao = utenteAgenziaDao;
        this.emailService = emailService;
    }

    /**
     * Effettua il login e ritorna il JWT
     */
    public LoginResponse login(LoginRequest request) {
        Utente utente;
        try {
            utente = utenteDao.findByEmail(request.getEmail());

            if (utente.isLocked()){
                throw new UnauthorizedException(ErrorCode.ACCOUNT_LOCKED);
            }
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), utente.getPassword())) {
            utente.setFailedLoginAttempts(utente.getFailedLoginAttempts() + 1);

            if (utente.getFailedLoginAttempts() >= 5) {
                utente.setLockedUntil(Instant.now().plus(15, java.time.temporal.ChronoUnit.MINUTES));
                inviaEmailDiNotifica(utente.getEmail());
            }
            utenteDao.updateLoginAttempts(utente);
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS);
        }

        utente.setFailedLoginAttempts(0);
        utente.setLockedUntil(null);
        utenteDao.updateLoginAttempts(utente);

        String token = jwtService.generateToken(utente.getUid(), utente.getRole(), utente.getEmail());
        return new LoginResponse(token, utente.getUid(), utente.getRole());
    }

    /**
     * Registrazione di un Cliente
     */
    public void registraCliente(RegistrazioneRequest request) {
        if (!request.getRole().equals("Cliente")){
            throw new BadRequestException(ErrorCode.INVALID_REGISTRATION_ROLE);
        }
        validatePasswordPolicy(request.getPassword(), request.getEmail());
        String uid = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        Utente utente = new Utente(uid, request.getEmail(), hashedPassword, "Cliente");
        try {
            utenteDao.save(utente);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_IN_USE);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    /**
     * Registrazione di Agente o Gestore (solo Admin)
     * Crea l'utente e lo associa a una agenzia via UtenteAgenzia
     */
    @Transactional
    public void registraGestoreOrAgente(String uidAdmin, RegistrazioneStaffRequest request) {
        if  (!request.getRole().equals("Gestore") && !request.getRole().equals("AgenteImmobiliare")){
            throw new BadRequestException(ErrorCode.INVALID_STAFF_ROLE);
        }
        int idAgenzia;
        try {
            idAgenzia = utenteAgenziaDao.getIdAgenzia(uidAdmin);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }

        String uid = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        String role = request.getRole();

        Utente utente = new Utente(uid, request.getEmail(), hashedPassword, role);
        try {
            utenteDao.save(utente);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }

        UtenteAgenzia utenteAgenzia = new UtenteAgenzia(idAgenzia, uid);
        try {
            utenteAgenziaDao.save(utenteAgenzia);
        } catch (org.springframework.dao.DataIntegrityViolationException e){
            throw new ConflictException(ErrorCode.USER_ALREADY_ASSOCIATED);
        } catch (org.springframework.dao.DataAccessException e){
            throw new InternalServerErrorException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    /**
     * Recupera l'email di un utente dato il suo uid
     */
    public String getEmailByUid(String uid) {
        try {
            return utenteDao.findEmailByUid(uid);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException(ErrorCode.EMAIL_NOT_FOUND);
        }
    }

    private static final String[] QWERTY_ROWS = {"qwertyuiop", "asdfghjkl", "zxcvbnm"};

    private void validatePasswordPolicy(String password, String email) {
        if (password == null || password.length() < 8 || password.length() > 255) {
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD);
        }
        if (password.contains(" ")) {
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD);
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD);
        }
        if (password.chars().noneMatch(Character::isLowerCase)) {
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD);
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD);
        }
        if (password.chars().allMatch(Character::isLetterOrDigit)) {
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD);
        }
        if (hasRepeatingChars(password, 3)) {
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD);
        }
        if (hasSequentialLetters(password, 5)) {
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD);
        }
        if (hasQwertySequence(password, 4)) {
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD);
        }
        if (containsEmailParts(password, email)) {
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD);
        }
    }

    private boolean hasRepeatingChars(String password, int minRepeat) {
        String lower = password.toLowerCase();
        int count = 1;
        for (int i = 1; i < lower.length(); i++) {
            if (lower.charAt(i) == lower.charAt(i - 1)) {
                count++;
                if (count >= minRepeat) return true;
            } else {
                count = 1;
            }
        }
        return false;
    }

    private boolean hasSequentialLetters(String password, int minLength) {
        String lower = password.toLowerCase();
        int count = 1;
        for (int i = 1; i < lower.length(); i++) {
            char prev = lower.charAt(i - 1);
            char curr = lower.charAt(i);
            if (Character.isLetter(prev) && Character.isLetter(curr) && Math.abs(curr - prev) == 1) {
                count++;
                if (count >= minLength) return true;
            } else {
                count = 1;
            }
        }
        return false;
    }

    private boolean hasQwertySequence(String password, int minLength) {
        String lower = password.toLowerCase();
        for (String row : QWERTY_ROWS) {
            for (int i = 0; i <= lower.length() - minLength; i++) {
                String window = lower.substring(i, i + minLength);
                String reversed = new StringBuilder(window).reverse().toString();
                if (row.contains(window) || row.contains(reversed)) return true;
            }
        }
        return false;
    }

    private boolean containsEmailParts(String password, String email) {
        if (email == null) return false;
        String lowerPassword = password.toLowerCase();
        String lowerEmail = email.toLowerCase();
        int atIndex = lowerEmail.indexOf('@');
        if (atIndex >= 4) {
            String localPart = lowerEmail.substring(0, atIndex);
            if (lowerPassword.contains(localPart)) return true;
        }
        if (atIndex >= 0) {
            String domain = lowerEmail.substring(atIndex + 1);
            for (String label : domain.split("\\.")) {
                if (label.length() >= 4 && lowerPassword.contains(label)) return true;
            }
        }
        return false;
    }

    private void inviaEmailDiNotifica(String destinatario){
        String oggetto = "Account Bloccato";
        String testo = "Il tuo account è stato bloccato per 15 min sulla piattaforma DietiEstates25 per troppi tentativi falliti d'accesso";

        emailService.inviaEmail(destinatario, oggetto, testo);
    }

}