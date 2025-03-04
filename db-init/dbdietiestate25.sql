--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2 (Debian 17.2-1.pgdg120+1)
-- Dumped by pg_dump version 17.2 (Debian 17.2-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: statoofferta; Type: TYPE; Schema: public; Owner: dietiestate25
--

CREATE TYPE public.statoofferta AS ENUM (
    'In Sospeso',
    'Accettata',
    'Rifiutata'
);


ALTER TYPE public.statoofferta OWNER TO dietiestate25;

--
-- Name: statovisita; Type: TYPE; Schema: public; Owner: dietiestate25
--

CREATE TYPE public.statovisita AS ENUM (
    'In Sospeso',
    'Confermata',
    'Rifiutata'
);


ALTER TYPE public.statovisita OWNER TO dietiestate25;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: agenzia; Type: TABLE; Schema: public; Owner: dietiestate25
--

CREATE TABLE public.agenzia (
    idagenzia integer NOT NULL
);


ALTER TABLE public.agenzia OWNER TO dietiestate25;

--
-- Name: agenzia_idagenzia_seq; Type: SEQUENCE; Schema: public; Owner: dietiestate25
--

CREATE SEQUENCE public.agenzia_idagenzia_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.agenzia_idagenzia_seq OWNER TO dietiestate25;

--
-- Name: agenzia_idagenzia_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dietiestate25
--

ALTER SEQUENCE public.agenzia_idagenzia_seq OWNED BY public.agenzia.idagenzia;


--
-- Name: immobile; Type: TABLE; Schema: public; Owner: dietiestate25
--

CREATE TABLE public.immobile (
    idimmobile integer NOT NULL,
    urlfoto character varying(255),
    descrizione text,
    prezzo double precision NOT NULL,
    dimensione integer NOT NULL,
    nbagni integer NOT NULL,
    nstanze integer NOT NULL,
    tipologia character varying(50) NOT NULL,
    latitudine double precision,
    longitudine double precision,
    indirizzo character varying(255),
    comune character varying(100),
    piano integer NOT NULL,
    hasascensore boolean NOT NULL,
    hasbalcone boolean NOT NULL,
    idagente uuid NOT NULL
);


ALTER TABLE public.immobile OWNER TO dietiestate25;

--
-- Name: immobile_idimmobile_seq; Type: SEQUENCE; Schema: public; Owner: dietiestate25
--

CREATE SEQUENCE public.immobile_idimmobile_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.immobile_idimmobile_seq OWNER TO dietiestate25;

--
-- Name: immobile_idimmobile_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dietiestate25
--

ALTER SEQUENCE public.immobile_idimmobile_seq OWNED BY public.immobile.idimmobile;


--
-- Name: offerta; Type: TABLE; Schema: public; Owner: dietiestate25
--

CREATE TABLE public.offerta (
    idofferta integer NOT NULL,
    importo double precision NOT NULL,
    stato public.statoofferta NOT NULL,
    idcliente uuid NOT NULL,
    idimmobile integer NOT NULL
);


ALTER TABLE public.offerta OWNER TO dietiestate25;

--
-- Name: offerta_idofferta_seq; Type: SEQUENCE; Schema: public; Owner: dietiestate25
--

CREATE SEQUENCE public.offerta_idofferta_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.offerta_idofferta_seq OWNER TO dietiestate25;

--
-- Name: offerta_idofferta_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dietiestate25
--

ALTER SEQUENCE public.offerta_idofferta_seq OWNED BY public.offerta.idofferta;


--
-- Name: utenteagenzia; Type: TABLE; Schema: public; Owner: dietiestate25
--

CREATE TABLE public.utenteagenzia (
    uid uuid NOT NULL,
    idagenzia integer NOT NULL,
    ruolo character varying(50) NOT NULL
);


ALTER TABLE public.utenteagenzia OWNER TO dietiestate25;

--
-- Name: visita; Type: TABLE; Schema: public; Owner: dietiestate25
--

CREATE TABLE public.visita (
    idvisita integer NOT NULL,
    data date NOT NULL,
    orario time without time zone NOT NULL,
    stato public.statovisita NOT NULL,
    idcliente uuid NOT NULL,
    idimmobile integer NOT NULL
);


ALTER TABLE public.visita OWNER TO dietiestate25;

--
-- Name: visita_idvisita_seq; Type: SEQUENCE; Schema: public; Owner: dietiestate25
--

CREATE SEQUENCE public.visita_idvisita_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.visita_idvisita_seq OWNER TO dietiestate25;

--
-- Name: visita_idvisita_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dietiestate25
--

ALTER SEQUENCE public.visita_idvisita_seq OWNED BY public.visita.idvisita;


--
-- Name: agenzia idagenzia; Type: DEFAULT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.agenzia ALTER COLUMN idagenzia SET DEFAULT nextval('public.agenzia_idagenzia_seq'::regclass);


--
-- Name: immobile idimmobile; Type: DEFAULT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.immobile ALTER COLUMN idimmobile SET DEFAULT nextval('public.immobile_idimmobile_seq'::regclass);


--
-- Name: offerta idofferta; Type: DEFAULT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.offerta ALTER COLUMN idofferta SET DEFAULT nextval('public.offerta_idofferta_seq'::regclass);


--
-- Name: visita idvisita; Type: DEFAULT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.visita ALTER COLUMN idvisita SET DEFAULT nextval('public.visita_idvisita_seq'::regclass);


--
-- Data for Name: agenzia; Type: TABLE DATA; Schema: public; Owner: dietiestate25
--

INSERT INTO public.agenzia (idagenzia) VALUES (1);


--
-- Data for Name: immobile; Type: TABLE DATA; Schema: public; Owner: dietiestate25
--



--
-- Data for Name: offerta; Type: TABLE DATA; Schema: public; Owner: dietiestate25
--



--
-- Data for Name: utenteagenzia; Type: TABLE DATA; Schema: public; Owner: dietiestate25
--

INSERT INTO public.utenteagenzia (uid, idagenzia, ruolo) VALUES ('e0cc497c-8031-70c0-6369-ecfbe86a0f2b', 1, 'Admin');


--
-- Data for Name: visita; Type: TABLE DATA; Schema: public; Owner: dietiestate25
--



--
-- Name: agenzia_idagenzia_seq; Type: SEQUENCE SET; Schema: public; Owner: dietiestate25
--

SELECT pg_catalog.setval('public.agenzia_idagenzia_seq', 1, true);


--
-- Name: immobile_idimmobile_seq; Type: SEQUENCE SET; Schema: public; Owner: dietiestate25
--

SELECT pg_catalog.setval('public.immobile_idimmobile_seq', 1, false);


--
-- Name: offerta_idofferta_seq; Type: SEQUENCE SET; Schema: public; Owner: dietiestate25
--

SELECT pg_catalog.setval('public.offerta_idofferta_seq', 1, false);


--
-- Name: visita_idvisita_seq; Type: SEQUENCE SET; Schema: public; Owner: dietiestate25
--

SELECT pg_catalog.setval('public.visita_idvisita_seq', 1, false);


--
-- Name: agenzia agenzia_pkey; Type: CONSTRAINT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.agenzia
    ADD CONSTRAINT agenzia_pkey PRIMARY KEY (idagenzia);


--
-- Name: immobile immobile_pkey; Type: CONSTRAINT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.immobile
    ADD CONSTRAINT immobile_pkey PRIMARY KEY (idimmobile);


--
-- Name: offerta offerta_pkey; Type: CONSTRAINT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.offerta
    ADD CONSTRAINT offerta_pkey PRIMARY KEY (idofferta);


--
-- Name: utenteagenzia utenteagenzia_pkey; Type: CONSTRAINT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.utenteagenzia
    ADD CONSTRAINT utenteagenzia_pkey PRIMARY KEY (uid);


--
-- Name: visita visita_pkey; Type: CONSTRAINT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.visita
    ADD CONSTRAINT visita_pkey PRIMARY KEY (idvisita);


--
-- Name: idx_immobile_dimensione_asc; Type: INDEX; Schema: public; Owner: dietiestate25
--

CREATE INDEX idx_immobile_dimensione_asc ON public.immobile USING btree (dimensione);


--
-- Name: idx_immobile_dimensione_desc; Type: INDEX; Schema: public; Owner: dietiestate25
--

CREATE INDEX idx_immobile_dimensione_desc ON public.immobile USING btree (dimensione DESC);


--
-- Name: idx_immobile_nbagni_asc; Type: INDEX; Schema: public; Owner: dietiestate25
--

CREATE INDEX idx_immobile_nbagni_asc ON public.immobile USING btree (nbagni);


--
-- Name: idx_immobile_nbagni_desc; Type: INDEX; Schema: public; Owner: dietiestate25
--

CREATE INDEX idx_immobile_nbagni_desc ON public.immobile USING btree (nbagni DESC);


--
-- Name: idx_immobile_nstanze_asc; Type: INDEX; Schema: public; Owner: dietiestate25
--

CREATE INDEX idx_immobile_nstanze_asc ON public.immobile USING btree (nstanze);


--
-- Name: idx_immobile_nstanze_desc; Type: INDEX; Schema: public; Owner: dietiestate25
--

CREATE INDEX idx_immobile_nstanze_desc ON public.immobile USING btree (nstanze DESC);


--
-- Name: idx_immobile_prezzo_asc; Type: INDEX; Schema: public; Owner: dietiestate25
--

CREATE INDEX idx_immobile_prezzo_asc ON public.immobile USING btree (prezzo);


--
-- Name: idx_immobile_prezzo_desc; Type: INDEX; Schema: public; Owner: dietiestate25
--

CREATE INDEX idx_immobile_prezzo_desc ON public.immobile USING btree (prezzo DESC);


--
-- Name: immobile fk_immobile_agente; Type: FK CONSTRAINT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.immobile
    ADD CONSTRAINT fk_immobile_agente FOREIGN KEY (idagente) REFERENCES public.utenteagenzia(uid);


--
-- Name: offerta fk_offerta_cliente; Type: FK CONSTRAINT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.offerta
    ADD CONSTRAINT fk_offerta_cliente FOREIGN KEY (idcliente) REFERENCES public.utenteagenzia(uid);


--
-- Name: offerta fk_offerta_immobile; Type: FK CONSTRAINT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.offerta
    ADD CONSTRAINT fk_offerta_immobile FOREIGN KEY (idimmobile) REFERENCES public.immobile(idimmobile);


--
-- Name: utenteagenzia fk_utenteagenzia_agenzia; Type: FK CONSTRAINT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.utenteagenzia
    ADD CONSTRAINT fk_utenteagenzia_agenzia FOREIGN KEY (idagenzia) REFERENCES public.agenzia(idagenzia);


--
-- Name: visita fk_visita_cliente; Type: FK CONSTRAINT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.visita
    ADD CONSTRAINT fk_visita_cliente FOREIGN KEY (idcliente) REFERENCES public.utenteagenzia(uid);


--
-- Name: visita fk_visita_immobile; Type: FK CONSTRAINT; Schema: public; Owner: dietiestate25
--

ALTER TABLE ONLY public.visita
    ADD CONSTRAINT fk_visita_immobile FOREIGN KEY (idimmobile) REFERENCES public.immobile(idimmobile);


--
-- PostgreSQL database dump complete
--

