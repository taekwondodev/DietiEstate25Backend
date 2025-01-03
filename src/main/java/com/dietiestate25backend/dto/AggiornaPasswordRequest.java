package com.dietiestate25backend.dto;

public class AggiornaPasswordRequest {
    private String newPassword;

    private String oldPassword;

    private String accessToken;

    public String getNewPassword() {
        return newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
