package com.dietiestate25backend.model;

import java.util.UUID;

public class Immobile {
    private int idImmobile;
    private final String urlFoto;
    private final String descrizione;
    private final double prezzo;
    private final int nBagni;
    private final int nStanze;
    private final String tipologia;
    private final double longitudine;
    private final double latitudine;
    private final String indirizzo;
    private final int piano;
    private final boolean hasAscensore;
    private final boolean hasBalcone;
    private final UUID idResponsabile;

    private Immobile(Builder builder) {
        this.idImmobile = builder.idImmobile;
        this.urlFoto = builder.urlFoto;
        this.descrizione = builder.descrizione;
        this.prezzo = builder.prezzo;
        this.nBagni = builder.nBagni;
        this.nStanze = builder.nStanze;
        this.tipologia = builder.tipologia;
        this.longitudine = builder.longitudine;
        this.latitudine = builder.latitudine;
        this.indirizzo = builder.indirizzo;
        this.piano = builder.piano;
        this.hasAscensore = builder.hasAscensore;
        this.hasBalcone = builder.hasBalcone;
        this.idResponsabile = builder.idResponsabile;
    }


    public static class Builder {
        private int idImmobile;
        private String urlFoto;
        private String descrizione;
        private double prezzo;
        private int nBagni;
        private int nStanze;
        private String tipologia;
        private double longitudine;
        private double latitudine;
        private String indirizzo;
        private int piano;
        private boolean hasAscensore;
        private boolean hasBalcone;
        private UUID idResponsabile;

        public Builder setIdImmobile(int idImmobile) {
            this.idImmobile = idImmobile;
            return this;
        }

        public Builder setUrlFoto(String urlFoto) {
            this.urlFoto = urlFoto;
            return this;
        }

        public Builder setDescrizione(String descrizione) {
            this.descrizione = descrizione;
            return this;
        }

        public Builder setPrezzo(double prezzo) {
            this.prezzo = prezzo;
            return this;
        }

        public Builder setNBagni(int nBagni) {
            this.nBagni = nBagni;
            return this;
        }

        public Builder setNStanze(int nStanze) {
            this.nStanze = nStanze;
            return this;
        }

        public Builder setTipologia(String tipologia) {
            this.tipologia = tipologia;
            return this;
        }

        public Builder setLongitudine(double longitudine) {
            this.longitudine = longitudine;
            return this;
        }

        public Builder setLatitudine(double latitudine) {
            this.latitudine = latitudine;
            return this;
        }

        public Builder setIndirizzo(String indirizzo) {
            this.indirizzo = indirizzo;
            return this;
        }

        public Builder setPiano(int piano) {
            this.piano = piano;
            return this;
        }

        public Builder setHasAscensore(boolean hasAscensore) {
            this.hasAscensore = hasAscensore;
            return this;
        }

        public Builder setHasBalcone(boolean hasBalcone) {
            this.hasBalcone = hasBalcone;
            return this;
        }

        public Builder setIdResponsabile(UUID idResponsabile) {
            this.idResponsabile = idResponsabile;
            return this;
        }

        public Immobile build() {
            return new Immobile(this);
        }
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

    public int getIdImmobile() {
        return idImmobile;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }
}
