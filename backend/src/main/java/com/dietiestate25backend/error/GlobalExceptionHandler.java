package com.dietiestate25backend.error;

import com.dietiestate25backend.error.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.EnumMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String INTERNAL_ERROR_MSG = "Errore interno del server";
    private static final Map<ErrorCode, String> MESSAGES = new EnumMap<>(ErrorCode.class);

    static {
        // 400 Bad Request
        MESSAGES.put(ErrorCode.INVALID_PAGE_SIZE,         "Size non può superare 100");
        MESSAGES.put(ErrorCode.INVALID_PRICE,             "Prezzi non possono essere negativi");
        MESSAGES.put(ErrorCode.INVALID_PRICE_RANGE,       "PrezzoMin non può essere maggiore di PrezzoMax");
        MESSAGES.put(ErrorCode.INVALID_DIMENSION,         "Dimensione non può essere negativa");
        MESSAGES.put(ErrorCode.INVALID_BATHROOMS,         "nBagni non può essere negativo");
        MESSAGES.put(ErrorCode.INVALID_RESPONSABILE,      "Responsabile non valido");
        MESSAGES.put(ErrorCode.INVALID_ADDRESS,           "Indirizzo non valido");
        MESSAGES.put(ErrorCode.INVALID_CLIENT_ID,         "Id cliente non valido");
        MESSAGES.put(ErrorCode.INVALID_VISIT_TIME,        "L'orario della visita deve essere tra le 08:00 e le 18:00");
        MESSAGES.put(ErrorCode.INVALID_USER_ID,           "Id utente non valido");
        MESSAGES.put(ErrorCode.INVALID_STATUS,            "Stato non valido");
        MESSAGES.put(ErrorCode.INVALID_OFFERTA_STATUS,    "Stato offerta non valido");
        MESSAGES.put(ErrorCode.INVALID_DATE_FORMAT,       "Formato data non valido. Utilizzare il formato YYYY-MM-DD");
        MESSAGES.put(ErrorCode.INVALID_DATE_RANGE,        "Le previsioni meteo sono disponibili solo per i prossimi 7 giorni");
        MESSAGES.put(ErrorCode.INVALID_COORDINATES,       "Coordinate non valide");
        MESSAGES.put(ErrorCode.INVALID_LATITUDE,          "Latitudine deve essere tra -90 e 90");
        MESSAGES.put(ErrorCode.INVALID_LONGITUDE,         "Longitudine deve essere tra -180 e 180");
        MESSAGES.put(ErrorCode.INVALID_CATEGORY,          "Categoria non supportata");
        MESSAGES.put(ErrorCode.INVALID_REGISTRATION_ROLE, "Solo i clienti possono registrarsi attraverso questo endpoint");
        MESSAGES.put(ErrorCode.INVALID_STAFF_ROLE,        "Il ruolo deve essere 'Gestore' o 'AgenteImmobiliare'");
        MESSAGES.put(ErrorCode.INVALID_PASSWORD,          "La password non rispetta i requisiti di sicurezza");
        MESSAGES.put(ErrorCode.ADDRESS_NOT_FOUND,         "Nessuna coordinata trovata per l'indirizzo fornito");
        MESSAGES.put(ErrorCode.DATE_NOT_IN_FORECAST,      "Data non trovata nelle previsioni");

        // 409 Conflict
        MESSAGES.put(ErrorCode.INVALID_IMMOBILE_ATTRIBUTES, "Attributi non validi");
        MESSAGES.put(ErrorCode.EMAIL_ALREADY_IN_USE,         "Email già in uso");
        MESSAGES.put(ErrorCode.USER_ALREADY_ASSOCIATED,      "Utente già associato");

        // 404 Not Found
        MESSAGES.put(ErrorCode.IMMOBILE_NOT_FOUND,  "Immobile non trovato");
        MESSAGES.put(ErrorCode.CLIENT_NOT_FOUND,    "Cliente non trovato");
        MESSAGES.put(ErrorCode.RESOURCE_NOT_FOUND,  "Risorsa non trovata");
        MESSAGES.put(ErrorCode.INVALID_CREDENTIALS, "Email o password non corrette");
        MESSAGES.put(ErrorCode.EMAIL_NOT_FOUND,     "Risorsa non trovata");
        MESSAGES.put(ErrorCode.OFFERTA_NOT_FOUND,   "Offerta non trovata");
        MESSAGES.put(ErrorCode.ADMIN_NOT_FOUND,     "Utente non trovato");

        // 401 Unauthorized
        MESSAGES.put(ErrorCode.ACCOUNT_LOCKED,          "Account bloccato. Contatta l'amministratore");
        MESSAGES.put(ErrorCode.UNAUTHORIZED,             "Non autorizzato");
        MESSAGES.put(ErrorCode.INVALID_TOKEN,            "Token non valido o scaduto");
        MESSAGES.put(ErrorCode.INSUFFICIENT_PERMISSIONS, "Non hai i permessi per eseguire questa operazione");

        // 500 Internal / Database / External
        MESSAGES.put(ErrorCode.DATABASE_WRITE_ERROR, INTERNAL_ERROR_MSG);
        MESSAGES.put(ErrorCode.DATABASE_READ_ERROR,  INTERNAL_ERROR_MSG);
        MESSAGES.put(ErrorCode.INTERNAL_ERROR,       INTERNAL_ERROR_MSG);
        MESSAGES.put(ErrorCode.EXTERNAL_SERVICE_ERROR, INTERNAL_ERROR_MSG);
    }

    private String getMessage(ErrorCode code) {
        return MESSAGES.getOrDefault(code, INTERNAL_ERROR_MSG);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequest(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getMessage(e.getErrorCode()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> handleConflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(getMessage(e.getErrorCode()));
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<String> handleInternalServerError(InternalServerErrorException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getMessage(e.getErrorCode()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException e) {
        if (e.getErrorCode() == ErrorCode.INSUFFICIENT_PERMISSIONS) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(getMessage(e.getErrorCode()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(getMessage(e.getErrorCode()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getMessage(e.getErrorCode()));
    }

    @ExceptionHandler(DatabaseErrorException.class)
    public ResponseEntity<String> handleDatabaseErrorException(DatabaseErrorException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getMessage(e.getErrorCode()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Richiesta non valida: body o content-type malformato");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Dati di input non validi");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }



    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parametro non valido");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(INTERNAL_ERROR_MSG);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Richiesta non valida: dati duplicati o formato non consentito");
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(org.springframework.dao.DataAccessException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(INTERNAL_ERROR_MSG);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(error -> error.getPropertyPath() + ": " + error.getMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Dati di input non validi");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleHttpMediaTypeNotSupportedException(org.springframework.web.HttpMediaTypeNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Content-Type non supportato");
    }
}