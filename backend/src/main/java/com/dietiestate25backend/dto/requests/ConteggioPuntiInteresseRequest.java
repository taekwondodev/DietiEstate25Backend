package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;
import java.util.List;

public class ConteggioPuntiInteresseRequest {
    @NotNull(message = "Latitudine non può essere null")
    @DecimalMin(value = "-90.0", message = "Latitudine deve essere >= -90")
    @DecimalMax(value = "90.0", message = "Latitudine deve essere <= 90")
    private Double latitudine;

    @NotNull(message = "Longitudine non può essere null")
    @DecimalMin(value = "-180.0", message = "Longitudine deve essere >= -180")
    @DecimalMax(value = "180.0", message = "Longitudine deve essere <= 180")
    private Double longitudine;

    @Positive(message = "Raggio deve essere positivo")
    @Max(value = 50000, message = "Raggio non può superare 50000 metri")
    private Integer raggio;

    @NotNull(message = "Categorie non può essere null")
    @NotEmpty(message = "Categorie non può essere vuota")
    @Size(min = 1, max = 10, message = "Categorie deve contenere tra 1 e 10 elementi")
    private List<String> categorie;

    public ConteggioPuntiInteresseRequest() {}

    public ConteggioPuntiInteresseRequest(Double latitudine, Double longitudine, Integer raggio, List<String> categorie) {
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.raggio = raggio;
        this.categorie = categorie;
    }

    public Double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(Double latitudine) {
        this.latitudine = latitudine;
    }

    public Double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(Double longitudine) {
        this.longitudine = longitudine;
    }

    public Integer getRaggio() {
        return raggio;
    }

    public void setRaggio(Integer raggio) {
        this.raggio = raggio;
    }

    public List<String> getCategorie() {
        return categorie;
    }

    public void setCategorie(List<String> categorie) {
        this.categorie = categorie;
    }
}
