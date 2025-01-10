package com.dietiestate25backend.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.dietiestate25backend.config.TokenUtils;
import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.dao.postgresimplements.ClientePostgres;
import com.dietiestate25backend.dto.AggiornaPasswordRequest;
import com.dietiestate25backend.dto.LoginResponse;
import com.dietiestate25backend.dto.RegistrazioneRequest;
import com.dietiestate25backend.dto.RegistrazioneResponse;
import com.dietiestate25backend.error.exception.*;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;

@Service
public class AuthService {
    private final UtenteDao clienteDao;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    private final CognitoIdentityProviderClient cognitoClient;    /// costruttore definito in AWS Config

    public AuthService(CognitoIdentityProviderClient cognitoClient, ClientePostgres clienteDao) {
        this.cognitoClient = cognitoClient;
        this.clienteDao = clienteDao;
    }

    public RegistrazioneResponse registrazione(final RegistrazioneRequest registrazioneRequest) {
        try {
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .secretHash(generateSecretHash(clientId, clientSecret, registrazioneRequest.getEmail()))
                    .username(registrazioneRequest.getEmail())
                    .clientId(clientId)
                    .password(registrazioneRequest.getPassword())
                    .build();

            SignUpResponse signUpResponse = cognitoClient.signUp(signUpRequest);
            logger.info("Utente registrato con successo {}", signUpResponse.codeDeliveryDetails());

            addUserToGroup(registrazioneRequest);
            saveClienteToDatabase(signUpResponse.userSub());

            return new RegistrazioneResponse(signUpResponse.userSub());
        } catch (CognitoIdentityProviderException e) {
            logger.error(e.awsErrorDetails().errorMessage());

            if (e.awsErrorDetails().errorCode().equals("UsernameExistsException")) {
                throw new ConflictException("Utente già registrato");
            }
            else {
                throw new InternalServerErrorException("Errore durante la registrazione", e);
            }
        }
    }

    public LoginResponse login(final RegistrazioneRequest request) {
        try {
            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(Map.of(
                            "USERNAME", request.getEmail(),
                            "PASSWORD", request.getPassword(),
                            "SECRET_HASH", generateSecretHash(clientId, clientSecret, request.getEmail())
                    ))
                    .build();

            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            logger.info("Utente loggato con successo {}", authResponse);

            AuthenticationResultType result = authResponse.authenticationResult();
            return new LoginResponse(result.accessToken(), result.idToken(), result.refreshToken());

        } catch (CognitoIdentityProviderException e) {
            logger.error(e.awsErrorDetails().errorMessage());

            String errorCode = e.awsErrorDetails().errorCode();
            if (errorCode.equals("NotAuthorizedException")) {
                throw new UnauthorizedException("Credenziali non valide");
            }
            else if (errorCode.equals("UserNotFoundException")) {
                throw new NotFoundException("username non trovato");
            }
            else {
                throw new InternalServerErrorException("Errore durante il login", e);
            }
        }
    }

    public ResponseEntity<Void> aggiornaPassword(final AggiornaPasswordRequest request) {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .accessToken(request.getAccessToken())
                .previousPassword(request.getOldPassword())
                .proposedPassword(request.getNewPassword())
                .build();

        DecodedJWT jwt = TokenUtils.validateToken(request.getAccessToken());

        //questo è il response
        cognitoClient.changePassword(changePasswordRequest);

        if (jwt.getClaim("username").isNull()) {
            logger.error("Username non trovato nel token");
            throw new NotFoundException("Username non trovato nel token");
        }
        else {
            String username = jwt.getClaim("username").asString();
            logger.info("Password aggiornata con successo: {}", username);
            return ResponseEntity.ok().build();
        }
    }

    private static String generateSecretHash(final String clientId, String clientSecret, String username) {
        try {
            final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
            SecretKeySpec signingKey = new SecretKeySpec(clientSecret.getBytes(), HMAC_SHA256_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal((username + clientId).getBytes());
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new InternalServerErrorException("Errore calcolando il SECRET_HASH", e);
        }
    }

    private void addUserToGroup(RegistrazioneRequest request) {
        AdminAddUserToGroupRequest adminAddUserToGroupRequest = AdminAddUserToGroupRequest.builder()
                .userPoolId(userPoolId)
                .username(request.getEmail())
                .groupName(request.getGroup())
                .build();

        cognitoClient.adminAddUserToGroup(adminAddUserToGroupRequest);
        logger.info("Utente aggiunto al gruppo");
    }

    private void saveClienteToDatabase(String uid) {
        clienteDao.save(uid);
    }
}
