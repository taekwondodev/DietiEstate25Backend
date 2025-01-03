package com.dietiestate25backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Service
public class AuthService {
    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;    /// costruttore definito in AWS Config

    public void registrazione() {
        // registrazione utente
    }

    public void login() {
        // login utente
    }
}
