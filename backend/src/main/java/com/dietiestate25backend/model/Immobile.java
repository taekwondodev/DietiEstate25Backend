package com.dietiestate25backend.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class Immobile {
    @Setter
    private int idImmobile;
    private final String urlFoto;
    private final String descrizione;
    private final double prezzo;
    private final int dimensione;
    private final int nBagni;
    private final int nStanze;
    private final String tipologia;
    private final double longitudine;
    private final double latitudine;
    private final String indirizzo;
    private final String comune;
    private final int piano;
    private final boolean hasAscensore;
    private final boolean hasBalcone;
    private final String idResponsabile;
}
