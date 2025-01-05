package com.dietiestate25backend.dto;

public class LoginResponse {
    private String accessToken;

    private String idToken;

    private String refreshToken;

    public LoginResponse(String accessToken, String idToken, String refreshToken) {
        this.accessToken = accessToken;
        this.idToken = idToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
