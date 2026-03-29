package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;

public class MeteoRequest {
    @NotBlank(message = "Latitudine non può essere vuota")
    private String latitudine;

    @NotBlank(message = "Longitudine non può essere vuota")
    private String longitudine;

    @NotBlank(message = "Data non può essere vuota")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Data deve essere nel formato YYYY-MM-DD")
    private String date;

    public MeteoRequest() {}

    public String getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(String latitudine) {
        this.latitudine = latitudine;
    }

    public String getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(String longitudine) {
        this.longitudine = longitudine;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
