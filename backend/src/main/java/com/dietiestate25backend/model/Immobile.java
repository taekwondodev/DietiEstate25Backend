package com.dietiestate25backend.model;

import java.util.UUID;

public class Immobile {
    private final String descrizione;
    private final double prezzo;
    private final int nBagni;
    private final int nStanze;
    private final String tipologia;
    private final Indirizzo indirizzo;
    private final String latitudine;
    private final String longitudine;
    private final String indirizzo;
    private final String numerocivico;
    private final TipoClasseEnergetica classeEnergetica;
    private final int piano;
    private final boolean hasAscensore;
    private final boolean hasBalcone;
    private UUID idResponsabile;

    public Immobile(String descrizione, double prezzo, int nBagni, int nStanze, String tipologia, Indirizzo indirizzo, String latitudine, String longitudine, TipoClasseEnergetica classeEnergetica, int piano, boolean hasAscensore, boolean hasBalcone, UUID idResponsabile) {
        this.descrizione = descrizione;
        this.prezzo = prezzo;
        this.nBagni = nBagni;
        this.nStanze = nStanze;
        this.tipologia = tipologia;
        this.indirizzo = indirizzo;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.classeEnergetica = classeEnergetica;
        this.piano = piano;
        this.hasAscensore = hasAscensore;
        this.hasBalcone = hasBalcone;
        this.idResponsabile = idResponsabile;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public int getnBagni() {
        return nBagni;
    }

    public int getnStanze() {
        return nStanze;
    }

    public String getTipologia() {
        return tipologia;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public String getNumeroCivico() {
        return numerocivico;
    }

    public String getLatitudine() {
        return latitudine;
    }

    public String getLongitudine() {
        return longitudine;
    }

    public TipoClasseEnergetica getClasseEnergetica() {
        return classeEnergetica;
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

    public UUID getIdResponsabile() {
        return idResponsabile;
    }

    public void setIdResponsabile(UUID idResponsabile) {
        this.idResponsabile = idResponsabile;
    }

    public boolean isValid() {
        return (
                descrizione == null || prezzo == 0 || nBagni == 0 || nStanze == 0 || tipologia == null ||
                indirizzo == null || indirizzo.getVia() == null || indirizzo.getComune() == null || indirizzo.getCap() == null ||
                classeEnergetica == null || classeEnergetica.getClasse() == null || piano == 0 ||
                idResponsabile == null
        );
    }
}
