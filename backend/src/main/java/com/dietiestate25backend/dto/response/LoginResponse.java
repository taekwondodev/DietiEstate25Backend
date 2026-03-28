package com.dietiestate25backend.dto.response;

public class LoginResponse {
    private String token;
    private String sub;
    private String role;

    public LoginResponse() {}

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
