package com.dietiestate25backend.service;

import com.dietiestate25backend.dao.postgresimplements.AdminPostgres;
import com.dietiestate25backend.dao.postgresimplements.UtenteAgenziaPostgres;
import com.dietiestate25backend.dto.requests.RegistrazioneRequest;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.model.UtenteAgenzia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {
    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    private final CognitoIdentityProviderClient cognitoClient;

    private final AdminPostgres adminPostgres;

    private final UtenteAgenziaPostgres utenteAgenziaPostgres;

    public AuthService(CognitoIdentityProviderClient cognitoClient, AdminPostgres adminPostgres, UtenteAgenziaPostgres utenteAgenziaPostgres) {
        this.cognitoClient = cognitoClient;
        this.adminPostgres = adminPostgres;
        this.utenteAgenziaPostgres = utenteAgenziaPostgres;
    }

    public String getEmailByUid(String uid) {
        try {
            AdminGetUserRequest request = AdminGetUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(uid)
                    .build();

            AdminGetUserResponse response = cognitoClient.adminGetUser(request);


            return response.userAttributes().stream()
                    .filter(attribute -> "email".equals(attribute.name()))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow(() -> new NotFoundException("Email non trovata per l'utente: " + uid));

        } catch (CognitoIdentityProviderException e) {
            throw new InternalServerErrorException("Errore durante il recupero dell'email", e);
        }
    }

    public void registraGestoreOrAgente(String uidAdmin, RegistrazioneRequest request){
        int idAgenziaAdmin = adminPostgres.getIdAgenzia(uidAdmin);
        String uidUtenteAgenzia = salvaToCognito(request);

        UtenteAgenzia utenteAgenzia = new UtenteAgenzia(uidUtenteAgenzia, idAgenziaAdmin, request.getRole());

        utenteAgenziaPostgres.save(utenteAgenzia);
    }

    private String salvaToCognito(RegistrazioneRequest request) {
        AttributeType attributeEmail = AttributeType.builder()
                .name("email")
                .value(request.getEmail())
                .build();
        AttributeType attributeRole = AttributeType.builder()
                .name("custom:role")
                .value(request.getRole())
                .build();

        List<AttributeType> attributes = new ArrayList<>();
        attributes.add(attributeEmail);
        attributes.add(attributeRole);

        try {
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .userAttributes(attributes)
                    .username(request.getEmail())
                    .clientId(clientId)
                    .password(request.getPassword())
                    .build();

            SignUpResponse signUpResponse = cognitoClient.signUp(signUpRequest);

            return signUpResponse.userSub();
        } catch (CognitoIdentityProviderException e) {
            throw new InternalServerErrorException("Errore durante la registrazione dell'utente", e);
        }
    }

}
