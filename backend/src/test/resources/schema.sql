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

CREATE TABLE public.agenzia (
                                idagenzia integer NOT NULL
);


CREATE SEQUENCE public.agenzia_idagenzia_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.agenzia_idagenzia_seq OWNED BY public.agenzia.idagenzia;
ALTER TABLE ONLY public.agenzia ALTER COLUMN idagenzia SET DEFAULT nextval('public.agenzia_idagenzia_seq'::regclass);

CREATE TABLE public.utenti (
                               uid   character varying(255)      NOT NULL,
                               email character varying(255)      NOT NULL,
                               password character varying(255)   NOT NULL,
                               role  character varying(50)       NOT NULL
);

CREATE TABLE public.utenteagenzia (
                                      uid       character varying(255) NOT NULL,
                                      idagenzia integer                NOT NULL
);

CREATE TABLE public.immobile (
                                 idimmobile    integer                NOT NULL,
                                 urlfoto       character varying(255),
                                 descrizione   text,
                                 prezzo        double precision       NOT NULL,
                                 dimensione    integer                NOT NULL,
                                 nbagni        integer                NOT NULL,
                                 nstanze       integer                NOT NULL,
                                 tipologia     character varying(50)  NOT NULL,
                                 latitudine    double precision,
                                 longitudine   double precision,
                                 indirizzo     character varying(255),
                                 comune        character varying(100),
                                 piano         integer                NOT NULL,
                                 hasascensore  boolean                NOT NULL,
                                 hasbalcone    boolean                NOT NULL,
                                 idagente      character varying(255)         NOT NULL
);


CREATE SEQUENCE public.immobile_idimmobile_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.immobile_idimmobile_seq OWNED BY public.immobile.idimmobile;
ALTER TABLE ONLY public.immobile ALTER COLUMN idimmobile SET DEFAULT nextval('public.immobile_idimmobile_seq'::regclass);

CREATE TABLE public.offerta (
                                idofferta  integer                NOT NULL,
                                importo    double precision       NOT NULL,
                                stato      public.statoofferta    NOT NULL,
                                idcliente  character varying(255)          NOT NULL,
                                idimmobile integer                NOT NULL
);


CREATE SEQUENCE public.offerta_idofferta_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.offerta_idofferta_seq OWNED BY public.offerta.idofferta;
ALTER TABLE ONLY public.offerta ALTER COLUMN idofferta SET DEFAULT nextval('public.offerta_idofferta_seq'::regclass);

CREATE TABLE public.visita (
                               idvisita   integer              NOT NULL,
                               data       date                 NOT NULL,
                               orario     time without time zone NOT NULL,
                               stato      public.statovisita   NOT NULL,
                               idcliente  character varying(255)           NOT NULL,
                               idimmobile integer              NOT NULL
);


CREATE SEQUENCE public.visita_idvisita_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.visita_idvisita_seq OWNED BY public.visita.idvisita;
ALTER TABLE ONLY public.visita ALTER COLUMN idvisita SET DEFAULT nextval('public.visita_idvisita_seq'::regclass);

ALTER TABLE ONLY public.agenzia
    ADD CONSTRAINT agenzia_pkey PRIMARY KEY (idagenzia);

ALTER TABLE ONLY public.utenti
    ADD CONSTRAINT utenti_pkey PRIMARY KEY (uid);

ALTER TABLE ONLY public.utenti
    ADD CONSTRAINT utenti_email_unique UNIQUE (email);

ALTER TABLE ONLY public.utenteagenzia
    ADD CONSTRAINT utenteagenzia_pkey PRIMARY KEY (uid);

ALTER TABLE ONLY public.immobile
    ADD CONSTRAINT immobile_pkey PRIMARY KEY (idimmobile);

ALTER TABLE ONLY public.offerta
    ADD CONSTRAINT offerta_pkey PRIMARY KEY (idofferta);

ALTER TABLE ONLY public.visita
    ADD CONSTRAINT visita_pkey PRIMARY KEY (idvisita);

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

CREATE INDEX idx_immobile_prezzo_asc      ON public.immobile USING btree (prezzo);
CREATE INDEX idx_immobile_prezzo_desc     ON public.immobile USING btree (prezzo DESC);
CREATE INDEX idx_immobile_dimensione_asc  ON public.immobile USING btree (dimensione);
CREATE INDEX idx_immobile_dimensione_desc ON public.immobile USING btree (dimensione DESC);
CREATE INDEX idx_immobile_nbagni_asc      ON public.immobile USING btree (nbagni);
CREATE INDEX idx_immobile_nbagni_desc     ON public.immobile USING btree (nbagni DESC);
CREATE INDEX idx_immobile_nstanze_asc     ON public.immobile USING btree (nstanze);
CREATE INDEX idx_immobile_nstanze_desc    ON public.immobile USING btree (nstanze DESC);

INSERT INTO public.agenzia (idagenzia) VALUES (1), (2);
SELECT pg_catalog.setval('public.agenzia_idagenzia_seq', 2, true);

INSERT INTO public.utenti (uid, email, password, role) VALUES
                                                           ('uid-admin-001',   'admin@test.it',    'password_test', 'Admin'),
                                                           ('uid-agente-001',  'agente1@test.it',  'password_test', 'AgenteImmobiliare'),
                                                           ('uid-gestore-002',  'gestore1@test.it',  'password_test', 'Gestore'),
                                                           ('uid-cliente-001', 'cliente1@test.it', 'password_test', 'Cliente'),
                                                           ('uid-cliente-002', 'cliente2@test.it', 'password_test', 'Cliente');

INSERT INTO public.utenteagenzia (uid, idagenzia) VALUES
                                                      ('uid-admin-001',  1),
                                                      ('uid-agente-001', 1),
                                                      ('uid-gestore-002', 1);

INSERT INTO public.immobile (urlfoto, descrizione, prezzo, dimensione, nbagni, nstanze, tipologia, latitudine, longitudine, indirizzo, comune, piano, hasascensore, hasbalcone, idagente) VALUES
                                                                                                                                                                                              ('https://test.it/foto1.jpg', 'Appartamento luminoso con vista',  250000.00, 80,  1, 3, 'Appartamento', 41.9028, 12.4964, 'Via Roma 1',    'Roma',   2, true,  true,  'uid-agente-001'),
                                                                                                                                                                                              ('https://test.it/foto2.jpg', 'Villetta con giardino privato',    450000.00, 150, 2, 5, 'Villa',        45.4642, 9.1900,  'Via Milano 10', 'Milano', 0, false, false, 'uid-agente-001'),
                                                                                                                                                                                              ('https://test.it/foto3.jpg', 'Monolocale in centro storico',     120000.00, 35,  1, 1, 'Monolocale',   40.8518, 14.2681, 'Via Napoli 5',  'Napoli', 3, true,  false, 'uid-gestore-002');
SELECT pg_catalog.setval('public.immobile_idimmobile_seq', 3, true);

INSERT INTO public.offerta (importo, stato, idcliente, idimmobile) VALUES
                                                                       (240000.00, 'In Sospeso', 'uid-cliente-001', 1),
                                                                       (430000.00, 'Accettata',  'uid-cliente-002', 2);
SELECT pg_catalog.setval('public.offerta_idofferta_seq', 2, true);

INSERT INTO public.visita (data, orario, stato, idcliente, idimmobile) VALUES
                                                                           ('2025-04-10', '10:00:00', 'Confermata', 'uid-cliente-001', 1),
                                                                           ('2025-04-12', '15:30:00', 'In Sospeso', 'uid-cliente-002', 3);
SELECT pg_catalog.setval('public.visita_idvisita_seq', 2, true);