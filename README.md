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
8. [Testing](#8-testing)
9. [Pipeline CI/CD](#9-pipeline-cicd)
10. [Qualità del Codice](#10-qualità-del-codice)

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

| Ruolo | Descrizione                                                |
|-------|------------------------------------------------------------|
| **Unauthenticated** | Utente non autenticato           |
| **Admin** | Amministratore di agenzia |
| **Gestore** | Responsabile operativo agenzia    |
| **AgenteImmobiliare** | Agente di commercializzazione  |
| **Cliente** | Utente finale   |

**Nota**: Per la matrice completa e dettagliata dei permessi per ogni endpoint, consultare la sezione [2.3](#23-matrice-di-accesso-endpoint).

### 2.3 Matrice di Accesso (Endpoint)

| Endpoint | Unauthenticated | Admin | Gestore | Agente | Cliente |
|----------|-----------------|-------|---------|--------|---------|
| `POST /auth/login` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `POST /auth/register` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `POST /auth/register-staff` | ✗ | ✓ | ✓ | ✗ | ✗ |
| `GET /immobile/cerca` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `POST /immobile/crea` | ✗ | ✓ | ✓ | ✓ | ✗ |
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

<div align="center">
  <img src="https://github.com/user-attachments/assets/29412d45-2306-4535-ae53-92c4f0da8756" alt="Class Diagram" style="width: 80%; max-width: 800px;">
</div>

Il database è composto da 6 entità principali:

- **Agenzia** — rappresenta l'agenzia immobiliare
- **Utente** — rappresenta un utente del sistema con email, password hashata e ruolo (Admin, Gestore, AgenteImmobiliare, Cliente)
- **UtenteAgenzia** — utenti dell'agenzia (Admin, Gestore, AgenteImmobiliare) con riferimento all'agenzia di appartenenza
- **Immobile** — inserzione immobiliare con coordinate geografiche, caratteristiche e url foto
- **Visita** — prenotazione di visita con stato (`IN_SOSPESO`, `CONFERMATA`, `RIFIUTATA`)
- **Offerta** — offerta economica con stato (`IN_SOSPESO`, `ACCETTATA`, `RIFIUTATA`)

Il database viene inizializzato automaticamente tramite lo script SQL nella cartella `db-init/`, montata come volume in Docker Compose.

---

## 6. Prerequisiti ed Installazione

### 6.1 Prerequisiti

- **Java 21+** (SDK)
- **Maven 3.8+**
- **Docker** e **Docker Compose**
- **OpenSSL** (per generare JWT secret)

### 6.2 Setup Iniziale

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

### 6.3 Avvio con Docker Compose

```bash
docker compose up --build -d
```

**Servizi in avvio**:
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432

---

## 7. Configurazione

### 7.1 File di Configurazione

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

### 7.2 Fail-Fast Configuration

In caso di variabili d'ambiente mancanti, l'applicazione fallisce al startup:

```java
// Spring Boot automaticamente valida le proprietà
// Se manca una variabile, lanciato: InvalidConfigurationPropertyException
```

---

## 8. Testing

### 8.1 Focus e Strategia

Il testing è **incentrato sulla sicurezza**, non sulla correttezza funzionale. L'obiettivo è verificare che il sistema sia **resistente ad attacchi e a input malevoli**, non che la business logic funzioni.

La strategia di test segue i principi di **OWASP Testing Guide** e **Security Testing Checklist**, concentrandosi su:

- **Input Validation & Boundary Testing** — Rifiuto di payload malformati, valori out-of-range
- **Authorization & Access Control** — RBAC, tenant isolation, boundary checks
- **Data Protection** — Hashing password, JWT validation, token tampering
- **Exception Handling** — Nessuna information leakage in caso di errori

### 8.2 Input Validation Testing

#### 8.2.1 MalformedPayloadTests (`security/validation/`)

Suite di **13 test** che verificano il corretto rifiuto di input malevoli:

##### JSON Parsing
- `testMalformedJson_InvalidSyntax_ShouldReturn400` — JSON syntax non valida
- `testEmptyBody_PostWithEmptyBody_ShouldReturn400` — Body vuoto
- `testEmptyObject_PostWithEmptyJsonObject_ShouldReturn400` — Oggetto JSON vuoto `{}`
- `testNullJson_SendingNullAsBody_ShouldReturn400` — Payload `null`
- `testArrayInsteadOfObject_ShouldReturn400` — Array `[]` invece di object

**Outcome**: Implementazione di `GlobalExceptionHandler` con handler per:
- `HttpMessageNotReadableException` → 400 Bad Request

##### Content Negotiation
- `testWrongContentType_TextPlainShouldNotBeAccepted` — Content-Type `text/plain` non accettato

**Outcome**: Spring Security valida Content-Type automaticamente

##### Buffer Overflow & Payload Size
- `testHugePayload_ExtremelyLargeShouldBeRejected` — Payload da 100,000 caratteri

**Outcome**: Implementazione di limiti di validazione nei DTO (`@Size`, `@DecimalMax`)

##### Type Mismatch
- `testNegativeIntegerParameters_ShouldBeHandled` — Parametri numerici negativi
- `testNonIntegerParameters_ShouldBeRejected` — Parametri non-numerici per campi interi
- `testDoubleParameters_InvalidFormatShouldBeRejected` — Parametri Double malformati

**Outcome**: Implementazione di `MethodArgumentTypeMismatchException` handler

##### Injection & Special Characters
- `testSpecialCharactersInString_ShouldNotCauseInjection` — SQL injection attempt: `'; DROP TABLE--`

**Outcome**: `@Email` validator rifiuta pattern sospetti. Parametrized queries in JDBC protegono ulteriormente

- `testUnicodeCharacters_ValidUnicodeShouldBeAccepted` — Unicode valido accettato

**Outcome**: Conferma che l'app supporta caratteri internazionali senza vulnerabilità

#### 8.2.2 LoginRequestValidationTests (`security/validation/`)

Suite di **10 test** che verificano la validazione specifica del DTO `LoginRequest`:

##### Field Presence & Nullability
- `testLoginRequest_MissingEmail_ShouldReturn400` — Campo `email` assente
- `testLoginRequest_MissingPassword_ShouldReturn400` — Campo `password` assente
- `testLoginRequest_NullEmail_ShouldReturn400` — Email `null`
- `testLoginRequest_NullPassword_ShouldReturn400` — Password `null`

**Outcome**: Annotazioni `@NotNull` e `@NotBlank` su `LoginRequest`

##### Field Content Validation
- `testLoginRequest_EmptyEmail_ShouldReturn400` — Email stringa vuota `""`
- `testLoginRequest_EmptyPassword_ShouldReturn400` — Password stringa vuota `""`
- `testLoginRequest_MalformedEmail_ShouldReturn400` — Email senza `@` domain (es. `not-an-email`)
- `testLoginRequest_EmailWithSpecialChars_ShouldReturn400` — Email con XSS payload: `user<script>alert('xss')</script>@example.com`
- `testLoginRequest_WhitespaceOnlyEmail_ShouldReturn400` — Email solo spazi: `"   "`
- `testLoginRequest_WhitespaceOnlyPassword_ShouldReturn400` — Password solo spazi: `"   "`

**Outcome**: Annotazioni `@Email` (RFC 5322), `@NotBlank`, trimming automatico

##### Attack Prevention
- `testLoginRequest_SqlInjectionInEmail_ShouldBeRejected` — SQL injection attempt: `'; DROP TABLE utenti; --` rifiutato dalla validazione email

**Outcome**: Validazione email + parametrized queries proteggono

- `testLoginRequest_ExtraFields_ShouldBeIgnored` — Campo extra `role: "Admin"` nella request ignorato (non injection)

**Outcome**: Jackson deserializer ignora automaticamente i campi non mappati

#### 8.2.3 RegistrazioneRequestValidationTests (`security/validation/`)

Suite di **10 test** che verificano la validazione specifica del DTO `RegistrazioneRequest`:

##### Field Presence & Nullability
- `testRegistrazioneRequest_MissingEmail_ShouldReturn400` — Campo `email` assente
- `testRegistrazioneRequest_MissingPassword_ShouldReturn400` — Campo `password` assente
- `testRegistrazioneRequest_MissingRole_ShouldReturn400` — Campo `role` assente

**Outcome**: Annotazioni `@NotNull` su `RegistrazioneRequest`

##### Field Content Validation
- `testRegistrazioneRequest_EmptyEmail_ShouldReturn400` — Email stringa vuota
- `testRegistrazioneRequest_EmptyPassword_ShouldReturn400` — Password stringa vuota
- `testRegistrazioneRequest_EmptyRole_ShouldReturn400` — Role stringa vuota
- `testRegistrazioneRequest_MalformedEmail_ShouldReturn400` — Email senza dominio
- `testRegistrazioneRequest_EmailWithXssAttempt_ShouldBeRejected` — Email con `<script>alert('xss')</script>@example.com`
- `testRegistrazioneRequest_WhitespaceOnlyEmail_ShouldReturn400` — Email solo spazi
- `testRegistrazioneRequest_WhitespaceOnlyPassword_ShouldReturn400` — Password solo spazi

**Outcome**: Annotazioni `@Email`, `@NotBlank` sul DTO

##### Attack Prevention & Edge Cases
- `testRegistrazioneRequest_SqlInjectionAttempt_ShouldBeRejected` — SQL injection in email rifiutato

**Outcome**: `@Email` validator + parametrized queries

- `testRegistrazioneRequest_ExtraFields_ShouldBeIgnored` — Campo extra `admin: true` nella request ignorato

**Outcome**: Jackson ignora campi non mappati

- `testRegistrazioneRequest_PasswordWithOnlyNumbers_ShouldBeAccepted` — Password numerica accettata (nessun requisito di complessità)

**Outcome**: Conferma che il sistema non ha validazione di password strength

#### 8.2.4 Miglioramenti Implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **Exception Handling** | Generic 500 errors | Typed 400/401/404 responses | Information hiding |
| **Input Validation - DTO** | `@NotNull` generic | `@NotBlank`, `@Size`, `@Pattern`, `@Email`, `@Min`/`@Max`, `@Positive` | Boundary protection |
| **Input Validation - Query Params** | Nessuna validazione | `@Min`, `@Positive`, `@NotBlank` su `@RequestParam` | Parameter tampering prevention |
| **Error Messages** | Implementazione-specific | Business-friendly, no tech leakage | Security by obscurity |
| **DAO Error Mapping** | `RuntimeException` uncaught | `DataIntegrityViolationException` → `ConflictException` | Constraint violation handling |
| **Type Coercion Safety** | No validation | `@MethodArgumentTypeMismatchException` handler | Type safety |

#### 8.2.3 Endpoint Coperto dalla Suite

| Endpoint              | Test Cases | Status |
|-----------------------|------------|--------|
| `POST /auth/login`    | 13         | ✅ Passing |
| `POST /auth/register` | 13         | ✅ Passing |
| `GET /immobile/cerca` | 5          | ✅ Passing |

### 8.3 Authorization & Access Control Testing

#### 8.3.1 PublicEndpointTests (`security/authorization/`)

Suite di **7 test** che verifica quali endpoint siano pubblici (non autenticati) e quali protetti:

##### Pubblici (con validazione ma senza JWT)
- `testLoginEndpoint_ShouldBePublic` — Non richiede JWT
- `testRegisterEndpoint_ShouldBePublic` — Non richiede JWT
- `testCercaImmobiliEndpoint_ShouldBePublic` — Non richiede JWT

**Outcome**: Configurazione `SecurityFilterChain` con `permitAll()` esplicito per endpoint pubblici

##### Protetti (richiedono JWT e autenticazione)
- `testRegisterStaffEndpoint_ShouldNotBePublic` — Richiede JWT
- `testImmobiliPersonaliEndpoint_ShouldNotBePublic` — Richiede JWT
- `testCreaImmobileEndpoint_ShouldNotBePublic` — Richiede JWT
- `testPrenotaVisitaEndpoint_ShouldNotBePublic` — Richiede JWT
- `testAggiungiOffertaEndpoint_ShouldNotBePublic` — Richiede JWT

**Outcome**: Fallback di Spring Security `anyRequest().authenticated()` funzionante correttamente

#### 8.3.2 AdminBoundaryTests (`security/authorization/`)

Suite di **6 test** che verifica che solo **Admin** (e Gestore) possano registrare nuovo staff:

- `testRegisterStaff_WithClienteRole_ShouldReturn401` — Cliente bloccato
- `testRegisterStaff_WithAgenteRole_ShouldReturn401` — AgenteImmobiliare bloccato
- `testRegisterStaff_WithGestoreRole_ShouldReturn200` — **Gestore piò registrare staff**
- `testRegisterStaff_WithAdminRole_ShouldReturn200` — **Admin può registrare staff**
- `testRegisterStaff_WithoutAuthentication_ShouldReturn401` — Unauthenticated bloccato
- `testRegisterStaff_WithInvalidRole_ShouldThrowException` — Role sconosciuto bloccato

**Outcome**: Implementazione di `TokenUtils.checkIfAdminOrGestore()` in `AuthController.registraGestoreOrAgente()`.

#### 8.3.3 UtenteAgenziaBoundaryTests (`security/authorization/`)

Suite di **10 test** che verifica che **solo UtenteAgenzia** (Admin, Gestore, AgenteImmobiliare) possano:
1. Creare immobili
2. Visualizzare i propri immobili

- `testCreateImmobile_WithClienteRole_ShouldReturn403` — Cliente **non può creare** immobili
- `testCreateImmobile_WithAdminRole_ShouldReturn201` — Admin **può creare**
- `testCreateImmobile_WithGestoreRole_ShouldReturn201` — Gestore **può creare**
- `testCreateImmobile_WithAgenteRole_ShouldReturn201` — AgenteImmobiliare **può creare**
- `testCreateImmobile_WithoutAuthentication_ShouldReturn401` — Unauthenticated rimandato
- `testImmobiliPersonali_WithClienteRole_ShouldReturn403` — Cliente **non può visualizzare**
- `testImmobiliPersonali_WithAdminRole_ShouldReturn200` — Admin **può visualizzare**
- `testImmobiliPersonali_WithGestoreRole_ShouldReturn200` — Gestore **può visualizzare**
- `testImmobiliPersonali_WithAgenteRole_ShouldReturn200` — AgenteImmobiliare **può visualizzare**
- `testImmobiliPersonali_WithoutAuthentication_ShouldReturn401` — Unauthenticated rimandato

**Outcome**: Implementazione di `TokenUtils.checkIfUtenteAgenzia()` in:
- `ImmobileController.creaImmobile()`
- `ImmobileController.immobiliPersonali()`
- `OffertaController.riepilogoOfferteUtenteAgenzia()`
- `VisitaController.riepilogoVisitaUtenteAgenzia()`

#### 8.3.4 Data Isolation Tests (`security/dataisolation/`)

Suite di **3 categorie di test** che verificano che gli endpoint "riepilogo UtenteAgenzia" siano **accessibili solo a UtenteAgenzia**.

##### ImmobileOwnershipTests (5 test)

Verifica che:
- Agente vede **solo i suoi immobili** nella lista personale
- Cliente **non può** accedere a `/immobile/personali` (403)
- Gestore e Admin **possono** accedere
- Ricerca pubblica (`/immobile/cerca`) è accessibile a tutti

**Outcome**: `checkIfUtenteAgenzia()` blocca Cliente da accesso

##### OffertaPrivacyTests (7 test)

Verifica la separazione dei dati tra offerte di Cliente e UtenteAgenzia:

- `testRiepilogoOfferteCliente_WithClienteRole_ShouldReturn200` — Cliente vede **solo le sue offerte**
- `testRiepilogoOfferteUtenteAgenzia_WithClienteRole_ShouldReturn403` — Cliente **bloccato** da riepilogo agenzia

**Outcome**: Implementazione di `TokenUtils.checkIfUtenteAgenzia()` in endpoint `/offerta/riepilogoUtenteAgenzia`

**Nota di Sicurezza**: Precedentemente questo endpoint era **accessibile anche ai Cliente**, permettendo la fuga di dati riservati. **Fixato con aggiunta della guardia di autorizzazione**.

- `testRiepilogoOfferteUtenteAgenzia_WithAdminRole_ShouldReturn200` — Admin accede
- `testRiepilogoOfferteUtenteAgenzia_WithGestoreRole_ShouldReturn200` — Gestore accede
- `testRiepilogoOfferteUtenteAgenzia_WithAgenteRole_ShouldReturn200` — Agente accede
- `testRiepilogoOfferteUtenteAgenzia_WithoutAuthentication_ShouldReturn401` — Unauthenticated bloccato

##### VisitaPrivacyTests (6 test)

Verifica la separazione dei dati tra visite di Cliente e UtenteAgenzia:

- `testRiepilogoVisiteCliente_WithClienteRole_ShouldReturn200` — Cliente vede **solo le sue visite**
- `testRiepilogoVisiteUtenteAgenzia_WithClienteRole_ShouldReturn403` — Cliente **bloccato** da riepilogo agenzia
- `testRiepilogoVisiteUtenteAgenzia_WithAdminRole_ShouldReturn200` — Admin accede
- `testRiepilogoVisiteUtenteAgenzia_WithGestoreRole_ShouldReturn200` — Gestore accede
- `testRiepilogoVisiteUtenteAgenzia_WithAgenteRole_ShouldReturn200` — Agente accede
- `testRiepilogoVisiteUtenteAgenzia_WithoutAuthentication_ShouldReturn401` — Unauthenticated bloccato

**Outcome**: Implementazione di `TokenUtils.checkIfUtenteAgenzia()` in `/visita/riepilogoUtenteAgenzia`

#### 8.3.5 Miglioramenti Implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **RBAC** | Nessun controllo di ruolo | `checkIfAdmin()`, `checkIfAdminOrGestore()`, `checkIfUtenteAgenzia()` | Authorization enforcement |
| **Public vs Protected** | Incoerente | Esplicitamente definito in `SecurityConfig` | Clear security model |
| **Endpoint Guarding** | Assente | Guardie in ogni endpoint sensibile | Access control |
| **Data Isolation** | Cliente poteva accedere riepilogoUtenteAgenzia | Bloccato con `checkIfUtenteAgenzia()` | Privacy protection |

#### 8.3.6 Endpoint Coperto dalla Suite (Fase 1 + Fase 2)

| Endpoint | Test Cases | Fase 1 | Fase 2 | Status |
|----------|------------|--------|--------|--------|
| `POST /auth/login` | 13         | ✅ | - | ✅ Passing |
| `POST /auth/register` | 13         | ✅ | - | ✅ Passing |
| `POST /auth/register-staff` | 6          | - | ✅ | ✅ Passing |
| `GET /immobile/cerca` | 5          | ✅ | ✅ | ✅ Passing |
| `POST /immobile/crea` | 5          | - | ✅ | ✅ Passing |
| `GET /immobile/personali` | 5          | - | ✅ | ✅ Passing |
| `GET /offerta/riepilogoCliente` | 1          | - | ✅ | ✅ Passing |
| `GET /offerta/riepilogoUtenteAgenzia` | 6          | - | ✅ | ✅ Passing |
| `GET /visita/riepilogoCliente` | 1          | - | ✅ | ✅ Passing |
| `GET /visita/riepilogoUtenteAgenzia` | 6          | - | ✅ | ✅ Passing |

---

### 8.4 Prossime Fasi (Roadmap)

#### Fase 3: JWT & Cryptography Tests
- [ ] Token tampering — Modifica payload/signature
- [ ] Token expiration — Token scaduto rifiutato
- [ ] Secret mismatch — Token firmato con secret errato
- [ ] BCrypt verification — Password hashing non compromesso

#### Fase 4: Business Logic Security Tests
- [ ] State transition validation (offerta: solo In Sospeso → Accettata/Rifiutata)
- [ ] Data ownership checks (cliente può modificare solo sue offerte)
- [ ] Cascade delete protection (eliminating immobile → cascata corretta)

#### Fase 5: Integration Tests
- [ ] End-to-end security flow (login → crea immobile → prenota visita)
- [ ] Database constraint validation
- [ ] Concurrent access handling

---

## 9. Pipeline CI/CD

🚧 **Work in progress** — La pipeline verrà configurata nelle prossime fasi.

### 9.1 Pianificazione

La pipeline avrà i seguenti stage:

1. **Build** — `mvn clean package`
2. **Security Tests** — `mvn test -Dgroups=security` (JUnit 5 tags)
3. **Integration Tests** — `mvn test -Dgroups=integration`
4. **Security Scan** — SAST (Static Application Security Testing)
5. **Docker Build** — Build immagine container
6. **Deploy to Staging** — Deployment test environment
7. **E2E Tests** — Test funzionali finali
8. **Deploy to Production** — Rilascio in prod (manual approval)

### 9.2 Trigger

- Ogni push su `security` branch
- Merge request / Pull request
- Tag di release

---

## 10. Qualità del Codice

### 10.1 Principi di Progettazione

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

### 10.2 SonarCloud Integration

Report attuale:

| Metrica | Voto | Dettagli |
|---------|------|----------|
| **Security** | A | 0 open issues |
| **Reliability** | A | 0 open issues |
| **Maintainability** | A | < 15 code smells |
| **Code Duplication** | 1.9% | ~2.3k LOC |

### 10.3 Refactoring Fearless

Non viene mantenuta backward compatibility per:
- Cambiamenti di entità (model)
- Signature di API
- Database schema

