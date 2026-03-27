# DietiEstates25Backend - Security Refactoring

Questo branch è dedicato al refactoring della sicurezza del sistema, con la rimozione di AWS Cognito e la gestione completa dell'autenticazione internamente tramite PostgreSQL e JWT con Spring Security.

> Per fare riferimento al README `precedente` all'avvio del progetto fare riferimento a [questo link](https://github.com/taekwondodev/DietiEstate25Backend/tree/main).

---

## Indice

1. [Descrizione dell'Applicazione](#1-descrizione-dellapplicazione)
2. [Architettura e Ruoli](#2-architettura-e-ruoli)
3. [Stack Tecnologico](#3-stack-tecnologico)
4. [Autenticazione e Sicurezza](#4-autenticazione-e-sicurezza)
5. [Schema del Database](#5-schema-del-database)
6. [Prerequisiti e Installazione](#6-prerequisiti-e-installazione)
7. [Configurazione](#7-configurazione)
8. [API Endpoints](#8-api-endpoints)
9. [Testing](#9-testing)
10. [Pipeline CI/CD](#10-pipeline-cicd)
11. [Qualità del Codice](#11-qualità-del-codice)

---

## 1. Descrizione dell'Applicazione

DietiEstates25 è una piattaforma per la gestione e commercializzazione di proprietà immobiliari. Le funzionalità principali includono:

- **Autenticazione** — Registrazione e login di clienti e operatori
- **Gestione Immobili** — Creazione, ricerca e visualizzazione proprietà con filtri avanzati
- **Prenotazioni Visite** — Prenotazione di visite in immobili con notifica email
- **Sistema di Offerte** — Offerte economiche su immobili con tracciamento dello stato
- **Servizi Geospaziali** — Integrazione con **Geoapify** per coordinate GPS e punti di interesse
- **Previsioni Meteo** — Integrazione con **Open Meteo** per verificare il meteo e facilitare la scelta dei giorni migliori in cui programmare una visita.
- **Gestione Agenziale** — Sistema multi-tenant per agenzie immobiliari con ruoli differenziati

---

## 2. Architettura e Ruoli

### 2.1 Struttura Organizzativa

Il sistema suddivide gli utenti in quattro categorie principali:

```
┌─────────────────────────────────────────┐
│         UTENTE (utenti)                 │
│   uid | email | password | role         │
└─────────────────────────────────────────┘
             1:N
       │
       ├─→ Admin             (Amministratore)
       ├─→ Gestore           (Gestore Agenzia)
       ├─→ AgenteImmobiliare (Agente Immobiliare)
       └─→ Cliente           (Cliente che cerca immobili)
       
┌──────────────────────────────────────────┐
│      UTENTE AGENZIA (utenteagenzia)      │
│         uid | idagenzia                  │
│                                          │
│      (Solo Admin, Gestore, Agente)       │
└──────────────────────────────────────────┘
```

### 2.2 Ruoli

| Ruolo | Descrizione                                                   |
|-------|---------------------------------------------------------------|
| **Unauthenticated** | Utente non autenticato — accesso pubblico limitato            |
| **Admin** | Amministratore di agenzia — gestione staff e immobili agenzia |
| **Gestore** | Responsabile operativo agenzia — gestione operativa           |
| **AgenteImmobiliare** | Agente di commercializzazione — gestione immobili propri      |
| **Cliente** | Utente finale — ricerca immobili, visite ed offerte           |

**Nota**: Per la matrice completa e dettagliata dei permessi per ogni endpoint, consultare la sezione [2.3](#23-matrice-di-accesso-endpoint).

### 2.3 Matrice di Accesso (Endpoint)

| Endpoint | Unauthenticated | Admin | Gestore | Agente | Cliente |
|----------|-----------------|-------|---------|--------|---------|
| `POST /auth/login` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `POST /auth/register` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `POST /auth/register-staff` | ✗ | ✓ | ✗ | ✗ | ✗ |
| `GET /immobile/cerca` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `POST /immobile/crea` | ✗ | ✓ | ✗ | ✓ | ✗ |
| `GET /immobile/personali` | ✗ | ✓ | ✓ | ✓ | ✗ |
| `POST /geodata` | ✗ | ✓ | ✓ | ✓ | ✓ |
| `POST /meteo` | ✗ | ✓ | ✓ | ✓ | ✓ |
| `POST /offerta/aggiungi` | ✗ | ✓ | ✓ | ✓ | ✓ |
| `PATCH /offerta/aggiorna` | ✗ | ✓ | ✓ | ✓ | ✓ |
| `GET /offerta/riepilogoCliente` | ✗ | ✓ | ✓ | ✓ | ✓ |
| `GET /offerta/riepilogoUtenteAgenzia` | ✗ | ✓ | ✓ | ✓ | ✗ |
| `POST /visita/prenota` | ✗ | ✓ | ✓ | ✓ | ✓ |
| `PATCH /visita/aggiorna` | ✗ | ✓ | ✓ | ✓ | ✓ |
| `GET /visita/riepilogoCliente` | ✗ | ✓ | ✓ | ✓ | ✓ |
| `GET /visita/riepilogoUtenteAgenzia` | ✗ | ✓ | ✓ | ✓ | ✗ |

---

## 3. Stack Tecnologico

| Tecnologia | Versione | Scopo |
|---|---|---|
| **Java** | 21 | Linguaggio principale |
| **Spring Boot** | 3.4.1 | Framework REST API |
| **Spring Data JDBC** | Latest | Accesso database |
| **Spring Security** | 6.2.4 | Autenticazione e autorizzazione |
| **Spring Validation** | Latest | Bean validation |
| **Spring Mail** | Latest | Invio notifiche email |
| **PostgreSQL** | Latest | Database relazionale |
| **Maven** | Latest | Build e gestione dipendenze |
| **Docker** | Latest | Containerizzazione applicazione |
| **JUnit 5 + Mockito** | Latest | Testing framework |

### 3.1 Dipendenze (pom.xml)

#### Rimosse (vs. branch `main`)
- `software.amazon.awssdk:cognitoidentityprovider`

#### Aggiunte (vs. branch `main`)
- `spring-boot-starter-security` — Hashing password BCrypt, autenticazione stateless
- `spring-security-test` — Testing di security

---

## 4. Autenticazione e Sicurezza

### 4.1 Flusso di Autenticazione

```
┌──────────────────────────────────┐
│  Client (Mobile App)             │
└────────────┬─────────────────────┘
             │
             │ 1. POST /auth/login
             │    { email, password }
             ↓
┌──────────────────────────────────┐
│  AuthController                  │
└────────────┬─────────────────────┘
             │
             │ 2. Lookup email in PostgreSQL
             ↓
┌──────────────────────────────────┐
│  UtentePostgres (DAO)            │
│  → findByEmail(email)            │
└────────────┬─────────────────────┘
             │
             │ 3. Verifica BCrypt password
             ↓
┌──────────────────────────────────┐
│  PasswordEncoder (BCrypt)        │
└────────────┬─────────────────────┘
             │
             │ 4. Genera JWT (HS256)
             ↓
┌──────────────────────────────────┐
│  JwtService                      │
│  → generateToken(uid, role, email)
└────────────┬─────────────────────┘
             │
             │ 5. Ritorna LoginResponse
             │    { token, uid, role }
             ↓
┌──────────────────────────────────┐
│  Client                          │
│  Authorization: Bearer <token>   │
│  (Token valido per 1 ora)        │
└──────────────────────────────────┘
```

### 4.2 Autenticazione con JWT

JSON Web Token (JWT) è uno standard aperto (RFC 7519) che definisce un formato compatto e auto-contenuto per trasmettere informazioni tra parti sotto forma di oggetto JSON.

**Motivazione della scelta**:

- **Stateless** — Il server non memorizza alcuna informazione di sessione. Ogni richiesta contiene tutte le informazioni necessarie.
- **Compatto** — Trasmissibile facilmente in header HTTP
- **Sicuro** — La firma impedisce tampering (alterazione del token)
- **Interoperabile** — Standard (JSON) ampiamente supportato da librerie in ogni linguaggio

**Flusso operativo**:
1. Client invia email + password
2. Server valida e crea JWT contenente: `uid`, `role`, `email`
3. Server ritorna il token al client
4. Client lo salva in memoria/storage locale
5. Client allega il token in ogni richiesta successiva (header `Authorization: Bearer <token>`)
6. Server valida la firma e l'integrità del token senza interrogare il database

---

### 4.3 Struttura del JWT e Payload

Il payload contiene i **claim**: dichiarazioni relative all'entità utente e a metadati del token. Nel nostro sistema:

```json
{
  "sub": "uid-agente-001",
  "iat": 1711420800,
  "exp": 1711424400,
  "role": "AgenteImmobiliare",
  "email": "agente1@test.it"
}
```

| Claim | Significato | Tipo |
|-------|-------------|------|
| `sub` | Subject — identificativo univoco (uid) dell'utente | String |
| `iat` | Issued At — timestamp di emissione | Long |
| `exp` | Expiration — timestamp di scadenza | Long |
| `role` | Custom claim — ruolo dell'utente | String |
| `email` | Custom claim — email dell'utente | String |

La **firma** è il meccanismo che garantisce l'integrità e l'autenticità del token. Nel nostro sistema utilizziamo l'algoritmo **HMAC-SHA256** con un secret condiviso (256 bit) per firmare il token.

**Garantisce**:
1. **Integrità** — Se un attaccante modifica anche un singolo carattere dell'header o del payload, la firma ricalcolata non corrisponderà più a quella originale, e il server rifiuterà il token.
2. **Autenticità** — Solo il server conosce la chiave segreta (secret_key). Un token firmato con la chiave corretta è stato necessariamente generato dal server (o da chi possiede la chiave), non da un client malevolo.
3. **Non ripudio** — Il server può dimostrare che il token è stato emesso da lui, poiché la chiave segreta non è condivisa con terze parti.

Il **TTL** è il periodo di validità del token dopo l'emissione, determinato dalla differenza tra `exp` e `iat`. Nel nostro sistema è impostato a 1 ora. Quindi dopo 1 ora, il token scade e non è più accettato dal server.
L'utente deve autenticarsi nuovamente per ottenere un nuovo token.

Poiché il JWT è un token **auto-contenuto** e **firmato**, il server può estrarre direttamente dal payload tutte le informazioni necessarie per l'autorizzazione, senza dover interrogare il database.

La firma HMAC-SHA256 garantisce che il contenuto del token non sia stato alterato dopo l'emissione. Una volta che il server ha verificato la firma, può considerare i claim presenti nel payload come attendibili — non c'è bisogno di verificarli nuovamente confrontandoli con i dati persistenti.

---

### 4.4 Assenza di Refresh Token

**Scelta progettuale**: Nel nostro sistema **non esiste il refresh token**. Questo è intenzionale e specificamente pensato per **app mobile**.

**Pro**:
- **Semplicità** — Meno complessità lato client e server
- **Meno superfici di attacco** — Un token in meno da proteggere
- **Esperienza UX accettabile** — Il TTL dura 1 ora
- **Meno stato** — Nessuna necessità di gestire una blacklist o un registro dei refresh token attivi

**Contro**:
- Se la sessione scade, il client deve re-autenticarsi (login di nuovo)
- Se un malintezionato ruba il token, può usarlo e rubare l'identità fino alla scadenza e non può essere revocato
- Il modello non è adatto ad applicazioni web

---

### 4.5 SecurityFilterChain

Il `SecurityFilterChain` è un componente centrale di Spring Security che agisce come **middleware di sicurezza** intermediario tra le richieste HTTP e il nostro codice applicativo. Implementa il pattern Filter Chain.

#### Funzionamento

Intercetta **ogni richiesta HTTP** e:
1. Valida il JWT (se presente)
2. Estrae le informazioni di autenticazione
3. Verifica l'autorizzazione (che il ruolo dell'utente possa accedere a quell'endpoint)
4. Se OK → propaga la richiesta al controller
5. Se FAIL → ritorna errore (401, 403)

#### Configurazione nel nostro sistema

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
    http
        .csrf(AbstractHttpConfigurer::disable)  // API mobile, CSRF non applicabile
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/login", "/auth/register", "/immobile/cerca").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .oauth2ResourceServer(oauth2 -> 
            oauth2.jwt(Customizer.withDefaults())
        );
    return http.build();
}
```

#### Spiegazione delle scelte

| Scelta | Valore | Motivo                                                                                                                            |
|--------|--------|-----------------------------------------------------------------------------------------------------------------------------------|
| **CSRF disable** | `AbstractHttpConfigurer::disable` | CSRF è un attacco web che richiede cookies e sessioni. Le API REST mobile non usano cookies, quindi la protezione CSRF è inutile. |
| **Endpoint pubblici** | `/auth/login`, `/auth/register`, `/immobile/cerca` | Specificati esplicitamente con `permitAll()`. Tutti gli altri endpoint richiederanno autenticazione.                              |
| **Session Policy** | `SessionCreationPolicy.STATELESS` | Il server non crea né utilizza sessioni HTTP. Ogni richiesta è indipendente, contiene tutte le informazioni necessarie            |
| **OAuth2 Resource Server** | JWT decoder abilitato | Spring Security sa come decodificare e validare JWT. La firma viene verificata automaticamente.                                   |
| **Fallback** | `anyRequest().authenticated()` | Qualsiasi endpoint non esplicitamente pubblico richiede autenticazione (JWT valido).                                              |

---

### 4.6 BCrypt — Hashing della Password

**BCrypt** è un algoritmo specificamente progettato per hashare password in modo sicuro. Non è un semplice hash — è un **key derivation function** che include salting e iterazioni.

```java
PasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode("password123");
// Output: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/TVm6
```

#### Proprietà di BCrypt

1. **Anti-timing attack** — Anche se la password è sbagliata, il tempo di verifica è simile. Evita che un attaccante possa dedurre informazioni dalla durata della risposta.

2. **Salting obbligatorio** — Il salt sono stringhe casuali aggiunte alla password. Protegge da rainbow table attacks e più password uguali hanno hash diverso.

3. **Community standard** — Usato da OWASP, Spring Security, la maggior parte dei framework moderni.

4. **Sicuro per default** — Non devi fare configurazioni critiche.

### 4.7 Generazione del JWT Secret

Il secret è generato con OpenSSL e salvato in formato base64 nel `.env`:

```bash
# Generare secret (256 bit = 32 byte)
openssl rand -base64 32

# Output: es. A9fTkL2xP8mQ4vB9sJ7wHn6kL3mN5qR2sT4uV6wX8yZ
```

Nel `SecurityConfig`, il secret viene convertito in 256 bit e usato come chiave HMAC:

```java
byte[] decodedSecret = Base64.getDecoder().decode(secret);
SecretKey key = new SecretKeySpec(decodedSecret, 0, decodedSecret.length, "HmacSHA256");
```

---

## 5. Schema del Database

**qui diagram**

Il database è composto da 6 entità principali:

- **Agenzia** — rappresenta l'agenzia immobiliare
- **Utente** — rappresenta un utente del sistema con email, password hashata e ruolo (Admin, Gestore, AgenteImmobiliare, Cliente)
- **UtenteAgenzia** — utenti dell'agenzia (Admin, Gestore, AgenteImmobiliare) con riferimento all'agenzia di appartenenza
- **Immobile** — inserzione immobiliare con coordinate geografiche, caratteristiche e url foto
- **Visita** — prenotazione di visita con stato (`IN_SOSPESO`, `CONFERMATA`, `RIFIUTATA`)
- **Offerta** — offerta economica con stato (`IN_SOSPESO`, `ACCETTATA`, `RIFIUTATA`)

Il database viene inizializzato automaticamente tramite lo script SQL nella cartella `db-init/`, montata come volume in Docker Compose.

---

## 7. Prerequisiti ed Installazione

### 7.1 Prerequisiti

- **Java 21+** (SDK)
- **Maven 3.8+**
- **Docker** e **Docker Compose**
- **OpenSSL** (per generare JWT secret)

### 7.2 Setup Iniziale

#### Passo 1: Clonare il Repository

```bash
git clone https://github.com/taekwondodev/DietiEstate25Backend.git
cd DietiEstate25Backend
git switch security
```

#### Passo 2: Generare il JWT Secret

```bash
# Generare un secret base64 256-bit
openssl rand -base64 32

# Output: Es. "A9fTkL2xP8mQ4vB9sJ7wHn6kL3mN5qR2sT4uV6wX8yZ="
```

#### Passo 3: Creare il File `.env`

Nella root del progetto, creare `.env`:

```env
# ==================== Database PostgreSQL (Docker Compose) ====================
POSTGRES_URL=jdbc:postgresql://postgres:5432/dietiestate25_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=dietiestate25_db
# ==================== JWT Secret ====================
JWT_SECRET=<INSERIRE_IL_SECRET_GENERATO>
# ==================== Server Configuration ====================
SERVER_PORT=8080
# ==================== Email Configuration (SMTP) ====================
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<TUA_EMAIL>
MAIL_PASSWORD=<TUA_APP_PASSWORD>
# ==================== External APIs ====================
GEO_KEY=<TUA_GEOAPIFY_API_KEY>
```

**Note su Email**:
- Per Gmail, generare una [App Password](https://myaccount.google.com/apppasswords) se 2FA abilitato
- Per altri provider, consultare la documentazione SMTP

### 7.3 Avvio con Docker Compose

```bash
docker compose up --build -d
```

**Servizi in avvio**:
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432

---

## 8. Configurazione

### 8.1 File di Configurazione

#### `application.properties`

Tutte le proprietà vengono injettate da variabili d'ambiente (12-factor app):

```properties
# Database
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# JWT
app.jwt.secret=${JWT_SECRET}

# Email
spring.mail.host=${MAIL_HOST}
spring.mail.port=${MAIL_PORT}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

# External APIs
GEO_KEY=${GEO_KEY}
```

#### `compose.yaml`

Orchestrazione dei servizi:

```yaml
services:
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=${POSTGRES_URL}
      # ... (altre variabili da .env)
    depends_on:
      - postgres

```

### 8.2 Fail-Fast Configuration

In caso di variabili d'ambiente mancanti, l'applicazione fallisce al startup:

```java
// Spring Boot automaticamente valida le proprietà
// Se manca una variabile, lanciato: InvalidConfigurationPropertyException
```

---

## 9. Testing

### 9.1 Stato Attuale

🚧 **Work in progress** — La suite di test sarà implementata nella fase successiva con focus su:

- **Security Tests** (Unit) — Autorizzazione, validazione JWT, boundary checks
- **Integration Tests** — DAO layer, Service layer, database transactions
- **End-to-End Tests** — Flussi completi di autenticazione e creazione immobili

### 9.2 Strategie di Testing Pianificate

#### Test di Sicurezza (Mandatory)

Concentrato su:
- Validazione JWT (scadenza, tampering, secret mismatch)
- RBAC (Role-Based Access Control)
- Tenant isolation (isolamento dati multi-tenant)
- Password hashing (BCrypt verification)
- Rejection di payload malformati

#### Test di Integrazione

- DAO layer con transazioni PostgreSQL
- Service layer logic
- End-to-end flow (Login → Crea Immobile → Prenota Visita)

---

## 10. Pipeline CI/CD

🚧 **Work in progress** — La pipeline verrà configurata nelle prossime fasi.

### 10.1 Pianificazione

La pipeline avrà i seguenti stage:

1. **Build** — `mvn clean package`
2. **Test** — `mvn test` (con coverage SonarQube)
3. **Security Scan** — SAST (Static Application Security Testing)
4. **Docker Build** — Build immagine container
5. **Deploy to Staging** — Deployment test environment
6. **E2E Tests** — Test funzionali finali
7. **Deploy to Production** — Rilascio in prod (manual approval)

### 10.2 Trigger

- Ogni push su `security` branch
- Merge request / Pull request
- Tag di release

---

## 11. Qualità del Codice

### 11.1 Principi di Progettazione

#### Type-Driven Design (TyDD)

Le constraints vengono codificate nel type system:
- Enum per stati (`StatoOfferta`, `StatoVisita`)
- Model immutabili con Builder pattern
- UUID per identità distribuite

#### Separazione delle Responsabilità

```
Controller → Handler HTTP
  ↓
Service → Business logic
  ↓
Repository (DAO) → Data persistence
  ↓
Database
```

#### Self-Documenting Code

Nomi chiari, zero commenti superflui:

```java
public void registraGestoreOrAgente(String uidAdmin, RegistrazioneRequest request) {
    // Il nome del metodo dice tutto
}
```

### 11.2 SonarCloud Integration

Report attuale:

| Metrica | Voto | Dettagli |
|---------|------|----------|
| **Security** | A | 0 open issues |
| **Reliability** | A | 0 open issues |
| **Maintainability** | A | < 15 code smells |
| **Code Duplication** | 1.9% | ~2.3k LOC |

### 11.3 Refactoring Fearless

Non viene mantenuta backward compatibility per:
- Cambiamenti di entità (model)
- Signature di API
- Database schema

