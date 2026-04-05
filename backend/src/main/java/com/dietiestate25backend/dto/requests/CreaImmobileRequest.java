package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreaImmobileRequest {
    @NotBlank(message = "URL foto non può essere vuota")
    @Size(max=500, message = "URL foto non può superare 500 caratteri")
    private String urlFoto;
    @NotBlank(message = "Descrizione non può essere vuota")
    @Size(min=1, max=254, message = "Descrizione deve essere tra 1 e 254 caratteri")
    private String descrizione;
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
}
