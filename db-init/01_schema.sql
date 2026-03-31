--
-- Schema DietiEstates25 - struttura database
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- ---------------------------------------------------------------------------
-- ENUM TYPES
-- ---------------------------------------------------------------------------

CREATE TYPE public.statoofferta AS ENUM (
    'In Sospeso',
    'Accettata',
    'Rifiutata'
);

CREATE TYPE public.statovisita AS ENUM (
    'In Sospeso',
    'Confermata',
    'Rifiutata'
);

SET default_tablespace = '';
SET default_table_access_method = heap;

-- ---------------------------------------------------------------------------
-- TABLE: agenzia
-- ---------------------------------------------------------------------------

CREATE TABLE public.agenzia (
    idagenzia integer NOT NULL
);

CREATE SEQUENCE public.agenzia_idagenzia_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.agenzia_idagenzia_seq OWNED BY public.agenzia.idagenzia;
ALTER TABLE ONLY public.agenzia ALTER COLUMN idagenzia SET DEFAULT nextval('public.agenzia_idagenzia_seq'::regclass);

-- ---------------------------------------------------------------------------
-- TABLE: utenti
-- ---------------------------------------------------------------------------

CREATE TABLE public.utenti (
    uid                 character varying(255)   NOT NULL,
    email               character varying(255)   NOT NULL,
    password            character varying(255)   NOT NULL,
    role                character varying(50)    NOT NULL,
    failedloginattempts integer                  DEFAULT 0,
    lockeduntil         timestamp with time zone
);

-- ---------------------------------------------------------------------------
-- TABLE: utenteagenzia
-- ---------------------------------------------------------------------------

CREATE TABLE public.utenteagenzia (
    uid       character varying(255) NOT NULL,
    idagenzia integer                NOT NULL
);

-- ---------------------------------------------------------------------------
-- TABLE: immobile
-- ---------------------------------------------------------------------------

CREATE TABLE public.immobile (
    idimmobile   integer                NOT NULL,
    urlfoto      character varying(255),
    descrizione  text,
    prezzo       double precision       NOT NULL,
    dimensione   integer                NOT NULL,
    nbagni       integer                NOT NULL,
    nstanze      integer                NOT NULL,
    tipologia    character varying(50)  NOT NULL,
    latitudine   double precision,
    longitudine  double precision,
    indirizzo    character varying(255),
    comune       character varying(100),
    piano        integer                NOT NULL,
    hasascensore boolean                NOT NULL,
    hasbalcone   boolean                NOT NULL,
    idagente     character varying(255) NOT NULL
);

CREATE SEQUENCE public.immobile_idimmobile_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.immobile_idimmobile_seq OWNED BY public.immobile.idimmobile;
ALTER TABLE ONLY public.immobile ALTER COLUMN idimmobile SET DEFAULT nextval('public.immobile_idimmobile_seq'::regclass);

-- ---------------------------------------------------------------------------
-- TABLE: offerta
-- ---------------------------------------------------------------------------

CREATE TABLE public.offerta (
    idofferta  integer                NOT NULL,
    importo    double precision       NOT NULL,
    stato      public.statoofferta    NOT NULL,
    idcliente  character varying(255) NOT NULL,
    idimmobile integer                NOT NULL
);

CREATE SEQUENCE public.offerta_idofferta_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.offerta_idofferta_seq OWNED BY public.offerta.idofferta;
ALTER TABLE ONLY public.offerta ALTER COLUMN idofferta SET DEFAULT nextval('public.offerta_idofferta_seq'::regclass);

-- ---------------------------------------------------------------------------
-- TABLE: visita
-- ---------------------------------------------------------------------------

CREATE TABLE public.visita (
    idvisita   integer                  NOT NULL,
    data       date                     NOT NULL,
    orario     time without time zone   NOT NULL,
    stato      public.statovisita       NOT NULL,
    idcliente  character varying(255)   NOT NULL,
    idimmobile integer                  NOT NULL
);

CREATE SEQUENCE public.visita_idvisita_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.visita_idvisita_seq OWNED BY public.visita.idvisita;
ALTER TABLE ONLY public.visita ALTER COLUMN idvisita SET DEFAULT nextval('public.visita_idvisita_seq'::regclass);

-- ---------------------------------------------------------------------------
-- PRIMARY KEYS
-- ---------------------------------------------------------------------------

ALTER TABLE ONLY public.agenzia      ADD CONSTRAINT agenzia_pkey      PRIMARY KEY (idagenzia);
ALTER TABLE ONLY public.utenti       ADD CONSTRAINT utenti_pkey        PRIMARY KEY (uid);
ALTER TABLE ONLY public.utenti       ADD CONSTRAINT utenti_email_unique UNIQUE (email);
ALTER TABLE ONLY public.utenteagenzia ADD CONSTRAINT utenteagenzia_pkey PRIMARY KEY (uid);
ALTER TABLE ONLY public.immobile     ADD CONSTRAINT immobile_pkey      PRIMARY KEY (idimmobile);
ALTER TABLE ONLY public.offerta      ADD CONSTRAINT offerta_pkey       PRIMARY KEY (idofferta);
ALTER TABLE ONLY public.visita       ADD CONSTRAINT visita_pkey        PRIMARY KEY (idvisita);

-- ---------------------------------------------------------------------------
-- FOREIGN KEYS
-- ---------------------------------------------------------------------------

ALTER TABLE ONLY public.utenteagenzia
    ADD CONSTRAINT fk_utenteagenzia_utenti FOREIGN KEY (uid)
        REFERENCES public.utenti(uid) ON DELETE CASCADE;

ALTER TABLE ONLY public.utenteagenzia
    ADD CONSTRAINT fk_utenteagenzia_agenzia FOREIGN KEY (idagenzia)
        REFERENCES public.agenzia(idagenzia);

ALTER TABLE ONLY public.immobile
    ADD CONSTRAINT fk_immobile_agente FOREIGN KEY (idagente)
        REFERENCES public.utenteagenzia(uid);

ALTER TABLE ONLY public.offerta
    ADD CONSTRAINT fk_offerta_cliente FOREIGN KEY (idcliente)
        REFERENCES public.utenti(uid);

ALTER TABLE ONLY public.offerta
    ADD CONSTRAINT fk_offerta_immobile FOREIGN KEY (idimmobile)
        REFERENCES public.immobile(idimmobile);

ALTER TABLE ONLY public.visita
    ADD CONSTRAINT fk_visita_cliente FOREIGN KEY (idcliente)
        REFERENCES public.utenti(uid);

ALTER TABLE ONLY public.visita
    ADD CONSTRAINT fk_visita_immobile FOREIGN KEY (idimmobile)
        REFERENCES public.immobile(idimmobile);

-- ---------------------------------------------------------------------------
-- INDEXES
-- ---------------------------------------------------------------------------

CREATE INDEX idx_immobile_prezzo_asc      ON public.immobile USING btree (prezzo);
CREATE INDEX idx_immobile_prezzo_desc     ON public.immobile USING btree (prezzo DESC);
CREATE INDEX idx_immobile_dimensione_asc  ON public.immobile USING btree (dimensione);
CREATE INDEX idx_immobile_dimensione_desc ON public.immobile USING btree (dimensione DESC);
CREATE INDEX idx_immobile_nbagni_asc      ON public.immobile USING btree (nbagni);
CREATE INDEX idx_immobile_nbagni_desc     ON public.immobile USING btree (nbagni DESC);
CREATE INDEX idx_immobile_nstanze_asc     ON public.immobile USING btree (nstanze);
CREATE INDEX idx_immobile_nstanze_desc    ON public.immobile USING btree (nstanze DESC);
