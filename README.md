# DietiEstates25Backend

Backend REST API per la piattaforma di gestione di servizi immobiliari **DietiEstates25**, sviluppata come progetto di Ingegneria del Software presso l'Università degli Studi di Napoli Federico II.

---

## Descrizione

Il backend espone una REST API stateless che gestisce le funzionalità principali della piattaforma: autenticazione degli utenti, ricerca e creazione di immobili, gestione di offerte e prenotazioni di visite, integrazione con servizi geospaziali (Geoapify) e meteo (Open Meteo).

---

## Tecnologie

| Tecnologia | Versione | Scopo |
|---|---|---|
| Java | 21 | Linguaggio principale |
| Spring Boot | 3.4.1 | Framework REST API |
| Spring Data JDBC | — | Accesso al database |
| Spring Security + OAuth2 | 6.2.4 | Autenticazione e autorizzazione |
| PostgreSQL | latest | Database relazionale |
| AWS Cognito | 2.25.52 | Gestione identità e autenticazione |
| Spring Mail | — | Notifiche email |
| Maven | — | Build e gestione dipendenze |
| Docker | — | Containerizzazione |

---

## Architettura

Il backend è strutturato in tre layer:

- **Controller** — gestisce le richieste HTTP in entrata ed espone i servizi REST
- **Service** — contiene la logica di business
- **DAO** — gestisce l'interazione con il database PostgreSQL via JDBC

L'autenticazione è basata su **JWT (JSON Web Token)** tramite Spring Security con AWS Cognito come Identity Provider. I dati sensibili (credenziali DB, API key) vengono gestiti tramite variabili d'ambiente in un file `.env`.

---

## Prerequisiti

- Java 21+
- Maven 3.8+
- Docker e Docker Compose
- Account AWS con Cognito configurato
- Chiave API Geoapify

---

## Installazione e avvio

### Con Docker Compose

1. Clona il repository:
   ```bash
   git clone https://github.com/taekwondodev/DietiEstate25Backend.git
   cd DietiEstate25Backend
   ```

2. Crea un file `.env` nella root del progetto con le seguenti variabili:
   ```env
   POSTGRES_URL=jdbc:postgresql://postgres:5432/dietiestates_db
   POSTGRES_USER=postgres
   POSTGRES_PASSWORD=postgres
   POSTGRES_DB=dietiestates_db
   GEO_KEY=<api_key_geoapify>
   ```

3. Avvia i container:
   ```bash
   docker compose up --build -d
   ```

Il backend sarà raggiungibile su `http://localhost:8080`.
Il database PostgreSQL sarà raggiungibile su `localhost:5432`.

---

## API Endpoints

Base URL: `http://localhost:8080`

### Autenticazione
| Metodo | Endpoint | Descrizione |
|---|---|---|
| `POST` | `/auth/register` | Registra un nuovo utente (gestore o agente) |

### Immobili
| Metodo | Endpoint | Descrizione |
|---|---|---|
| `GET` | `/immobile/cerca` | Cerca immobili per comune e filtri opzionali |
| `POST` | `/immobile/crea` | Crea un nuovo immobile |
| `GET` | `/immobile/personali` | Ottieni gli immobili dell'utente autenticato |

### Offerte
| Metodo | Endpoint                          | Descrizione                                                                  |
|---|-----------------------------------|------------------------------------------------------------------------------|
| `POST` | `/offerta/aggiungi`               | Invia una nuova offerta su un immobile                                       |
| `PATCH` | `/offerta/aggiorna`               | Aggiorna lo stato di un'offerta                                              |
| `GET` | `/offerta/riepilogoCliente`       | Riepilogo delle offerte del cliente                                          |
| `GET` | `/offerta/riepilogoUtenteAgenzia` | Riepilogo delle offerte agli immobili di cui è responsabile l'utente agenzia |

### Visite
| Metodo | Endpoint                         | Descrizione |
|---|----------------------------------|---|
| `POST` | `/visita/prenota`                | Prenota una visita per un immobile |
| `PATCH` | `/visita/aggiorna`               | Aggiorna lo stato di una visita |
| `GET` | `/visita/riepilogoCliente`       | Riepilogo delle visite del cliente |
| `GET` | `/visita/riepilogoUtenteAgenzia` | Riepilogo delle visite agli immobili di cui è responsabile l'utente agenzia |

### Servizi esterni
| Metodo | Endpoint | Descrizione |
|---|---|---|
| `POST` | `/geodata/conteggio-pdi` | Conta i punti di interesse vicini a un immobile (Geoapify) |
| `POST` | `/api/meteo` | Ottieni previsioni meteo per una posizione (Open Meteo) |

Per la specifica completa degli schemi di richiesta e risposta, consulta il file [`openapi.yaml`](backend/openapi.yaml).

---

## Schema del database

<div align="center">
  <img src="https://github.com/user-attachments/assets/d1296a8e-4ba0-40d7-a691-312833ef6d1a" alt="Class Diagram" style="width: 80%; max-width: 800px;">
</div>

Il database è composto da 5 entità principali:

- **Agenzia** — rappresenta l'agenzia immobiliare
- **UtenteAgenzia** — utenti dell'agenzia con ruolo (`ADMIN`, `GESTORE`, `AGENTE`)
- **Immobile** — inserzione immobiliare con coordinate geografiche, caratteristiche e foto
- **Visita** — prenotazione di visita con stato (`IN_SOSPESO`, `CONFERMATA`, `RIFIUTATA`)
- **Offerta** — offerta economica con stato (`IN_SOSPESO`, `ACCETTATA`, `RIFIUTATA`)

Il database viene inizializzato automaticamente tramite gli script SQL nella cartella `db-init/`, montata come volume in Docker Compose.

---

## Qualità del codice

La qualità del codice è monitorata con **SonarCloud**. Il report attuale mostra:

- **Security**: A — 0 open issues
- **Reliability**: A — 0 open issues
- **Maintainability**: A — 13 open issues
- **Duplicazioni**: 1.9% su ~2.3k righe

---


## Frontend

Link Frontend: [https://github.com/RinoTheNicePlayer/DietiEstates2025]
