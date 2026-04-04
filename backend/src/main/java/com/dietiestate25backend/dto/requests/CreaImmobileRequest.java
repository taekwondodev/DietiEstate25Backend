package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;

public class CreaImmobileRequest {
    @NotBlank(message = "Descrizione non può essere vuota")
    @Size(min=1, max=254, message = "Descrizione deve essere tra 1 e 254 caratteri")
    private String descrizione;
    @NotBlank(message = "URL foto non può essere vuota")
    @Size(max=500, message = "URL foto non può superare 500 caratteri")
    private String urlFoto;
    @Positive
    @DecimalMax(value="9999999.99", message = "Prezzo troppo grande")
    private double prezzo;
    @Positive
    @DecimalMax(value="99999.99", message = "Dimensione troppo grande")
    private int dimensione;
    @Positive
    @Max(value=15, message = "nBagni non può superare 15")
    private int nBagni;
    @Positive
    @Max(value=50, message = "nStanze non può superare 50")
    private int nStanze;
    @NotBlank(message = "Tipologia non può essere vuota")
    private String tipologia;
    @NotBlank(message = "Indirizzo non può essere vuoto")
    @Size(min=5, max=255, message = "Indirizzo deve essere tra 5 e 255 caratteri")
    private String indirizzo;
    @NotBlank(message = "Comune non può essere vuoto")
    @Size(min = 2, max = 100, message = "Comune deve essere tra 2 e 100 caratteri")
    private String comune;
    @Min(value=-1, message = "Piano non può essere inferiore a -1")
    @Max(value=100, message = "Piano non può essere superiore a 100")
    private int piano;
    @NotNull(message = "hasAscensore non può essere null")
    private boolean hasAscensore;
    @NotNull(message = "hasBalcone non può essere null")
    private boolean hasBalcone;

    public CreaImmobileRequest() {}

    public CreaImmobileRequest(String urlFoto, String descrizione, double prezzo, double dimensione,
                               int nBagni, int nStanze, String tipologia, String indirizzo,
                               String comune, int piano, boolean hasAscensore, boolean hasBalcone) {
        this.urlFoto = urlFoto;
        this.descrizione = descrizione;
        this.prezzo = prezzo;
        this.dimensione = (int) dimensione;
        this.nBagni = nBagni;
        this.nStanze = nStanze;
        this.tipologia = tipologia;
        this.indirizzo = indirizzo;
        this.comune = comune;
        this.piano = piano;
        this.hasAscensore = hasAscensore;
        this.hasBalcone = hasBalcone;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public int getDimensione() {
        return dimensione;
    }

    public int getNBagni() {
        return nBagni;
    }

    public int getNStanze() {
        return nStanze;
    }

    public String getTipologia() {
        return tipologia;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public String getComune() {
        return comune;
    }

    public int getPiano() {
        return piano;
    }

    public boolean isHasAscensore() {
        return hasAscensore;
    }

    public boolean isHasBalcone() {
        return hasBalcone;
    }

    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }
    public void setPrezzo(double prezzo) { this.prezzo = prezzo; }
    public void setDimensione(int dimensione) { this.dimensione = dimensione; }
    public void setNBagni(int nBagni) { this.nBagni = nBagni; }
    public void setNStanze(int nStanze) { this.nStanze = nStanze; }
    public void setTipologia(String tipologia) { this.tipologia = tipologia; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }
    public void setComune(String comune) { this.comune = comune; }
    public void setPiano(int piano) { this.piano = piano; }
    public void setHasAscensore(boolean hasAscensore) { this.hasAscensore = hasAscensore; }
    public void setHasBalcone(boolean hasBalcone) { this.hasBalcone = hasBalcone; }
}
