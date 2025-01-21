package com.dietiestate25backend.service;

import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.error.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

@Service
public class AuthService {
    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    private final CognitoIdentityProviderClient cognitoClient;

    public AuthService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
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
}
