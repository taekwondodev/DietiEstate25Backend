package com.dietiestate25backend.dto.response;

public class LoginResponse {
    private final String token;
    private final String sub;
    private final String role;

    public LoginResponse(String token, String sub, String role) {
        this.token = token;
        this.sub = sub;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getSub() {
        return sub;
    }

    public String getRole() {
        return role;
    }
}
