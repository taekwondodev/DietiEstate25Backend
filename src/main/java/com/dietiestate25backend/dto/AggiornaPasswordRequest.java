package com.dietiestate25backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AggiornaPasswordRequest {
    @NotBlank(message = "La nuova password non puo essere vuota")
    @Size(min = 8, message = "La nuova password deve essere almeno di 8 caratteri")
    private String newPassword;

    @NotBlank(message = "La vecchia password non puo essere vuota")
    private String oldPassword;

    @NotBlank(message = "Access token non puo essere vuoto")
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
