package com.dietiestate25backend.dto;

public class RegistrazioneRequest {
    private String email;
    private String password;
    private String group;

    public boolean isValid(){
        return email != null && password != null;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getGroup() {
        return group;
    }
}
