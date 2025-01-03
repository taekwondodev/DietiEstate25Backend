package com.dietiestate25backend.service;

import com.dietiestate25backend.dto.AggiornaPasswordRequest;
import com.dietiestate25backend.dto.LoginResponse;
import com.dietiestate25backend.dto.RegistrazioneRequest;
import com.dietiestate25backend.dto.RegistrazioneResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;    /// costruttore definito in AWS Config

    public RegistrazioneResponse registrazione(final RegistrazioneRequest registrazioneRequest) {
        if (!registrazioneRequest.isValid()) {
            logger.error("Email o password non possono essere nulli");
            throw new IllegalArgumentException("Email o password non possono essere nulli");
        }

        try {
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .secretHash(generateSecretHash(clientId, clientSecret, registrazioneRequest.getEmail()))
                    .username(registrazioneRequest.getEmail())
                    .clientId(clientId)
                    .password(registrazioneRequest.getPassword())
                    .build();

            SignUpResponse signUpResponse = cognitoClient.signUp(signUpRequest);
            logger.info("Utente registrato con successo {}", signUpResponse.codeDeliveryDetails());

            return new RegistrazioneResponse(signUpResponse.userSub());
        } catch (CognitoIdentityProviderException e) {
            logger.error(e.awsErrorDetails().errorMessage());
            throw new RuntimeException(e.awsErrorDetails().errorMessage(), e);
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
            throw new RuntimeException(e.awsErrorDetails().errorMessage(), e);
        }
    }

    public ResponseEntity<Void> aggiornaPassword(final AggiornaPasswordRequest request){
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .accessToken(request.getAccessToken())
                .previousPassword(request.getOldPassword())
                .proposedPassword(request.getNewPassword())
                .build();

        DecodedJWT jwt = JWT.decode(request.getAccessToken());

        ChangePasswordResponse changePasswordResponse = cognitoClient.changePassword(changePasswordRequest);
        logger.info("Password aggiornata con successo: {}", jwt.getClaim("username").asString());

        return ResponseEntity.ok().build();
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
            throw new RuntimeException("Error while calculating SECRET_HASH", e);
        }
    }
}
