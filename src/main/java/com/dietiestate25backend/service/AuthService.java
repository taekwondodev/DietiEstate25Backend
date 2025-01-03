package com.dietiestate25backend.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentity.model.CognitoIdentityProvider;

@Service
public class AuthService {
    private final CognitoIdentityProvider cognitoIdentityProvider;

    public AuthService(CognitoIdentityProvider cognitoIdentityProvider) {
        this.cognitoIdentityProvider = cognitoIdentityProvider;
    }
}
