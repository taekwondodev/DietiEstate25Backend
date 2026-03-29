package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;

public class CreaOffertaRequest {
    @Positive(message = "Importo deve essere positivo")
    @DecimalMax(value = "999999999.99", message = "Importo troppo grande")
    private double importo;

    @Positive(message = "ID immobile deve essere positivo")
    @Max(value = 999999, message = "ID immobile non valido")
    private int idImmobile;

    public CreaOffertaRequest() {}

    public CreaOffertaRequest(double importo, int idImmobile) {
        this.importo = importo;
        this.idImmobile = idImmobile;
    }

    public double getImporto() {
        return importo;
    }

    public void setImporto(double importo) {
        this.importo = importo;
    }

    public int getIdImmobile() {
        return idImmobile;
    }

    public void setIdImmobile(int idImmobile) {
        this.idImmobile = idImmobile;
    }
}
