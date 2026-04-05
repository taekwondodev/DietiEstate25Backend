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
6. [Testing](#6-testing)
7. [Pipeline](#7-pipeline)
8. [Setup Iniziale](#8-setup-iniziale)
9. [Qualità del Codice](#9-qualità-del-codice)

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

placeholder -> da finire

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

## 6. Testing

### 6.1 Focus e Strategia

Il testing è **incentrato sulla sicurezza**, non sulla correttezza funzionale. L'obiettivo è verificare che il sistema sia **resistente ad attacchi e a input malevoli**, non che la business logic funzioni.

La strategia di test segue i principi di **OWASP Testing Guide** e **Security Testing Checklist**, concentrandosi su:

- **Input Validation & Boundary Testing** — Rifiuto di payload malformati, valori out-of-range
- **Authorization & Access Control** — RBAC, tenant isolation, boundary checks, API security, brute force, account lockout
- **Data Protection** — Hashing password, password policy, JWT validation, token tampering
- **Business Logic Testing** — Business logic test, nessuna information leakage in caso di errori

### 6.2 Input Validation & Boundary Testing

#### 6.2.1 MalformedPayloadTests (`security/validation/`)

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
- `testWrongContentType_FormUrlencodedShouldReturn415` — Content-Type `application/x-www-form-urlencoded` non accettato (→ 415 Unsupported Media Type)

**Outcome**: Spring ritorna 415 quando il Content-Type non è `application/json`

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

- `testUnicodeCharacters_UnicodeShouldBeRejected` — Unicode rifiutato (→ 400 Bad Request)

**Outcome**: `@Pattern(regexp = "^[\\x20-\\x7E]+$")` su `LoginRequest.password` rifiuta caratteri non-ASCII

#### 6.2.2 LoginRequestValidationTests (`security/validation/`)

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

- `testLoginRequest_ExtraFields_ShouldReturn400` — Campo extra `role: "Admin"` nella request rifiutato (→ 400 Bad Request)

**Outcome**: Jackson configurato con `FAIL_ON_UNKNOWN_PROPERTIES=true` rifiuta campi non mappati

#### 6.2.3 RegistrazioneRequestValidationTests (`security/validation/`)

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

- `testRegistrazioneRequest_ExtraFields_ShouldReturn400` — Campo extra `admin: true` nella request rifiutato (→ 400 Bad Request)

**Outcome**: Jackson configurato con `FAIL_ON_UNKNOWN_PROPERTIES=true` rifiuta campi non mappati

- `testRegistrazioneRequest_PasswordWithOnlyNumbers_ShouldBeRejected` — Password numerica rifiutata

**Outcome**: Conferma che il sistema non ha validazione di password strength

#### 6.2.4 Miglioramenti Implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **Exception Handling** | Generic 500 errors | Typed 400/401/404 responses | Information hiding |
| **Input Validation - DTO** | `@NotNull` generic | `@NotBlank`, `@Size`, `@Pattern`, `@Email`, `@Min`/`@Max`, `@Positive` | Boundary protection |
| **Input Validation - Query Params** | Nessuna validazione | `@Min`, `@Positive`, `@NotBlank` su `@RequestParam` | Parameter tampering prevention |
| **Error Messages** | Implementazione-specific | Business-friendly, no tech leakage | Security by obscurity |
| **DAO Error Mapping** | `RuntimeException` uncaught | `DataIntegrityViolationException` → `ConflictException` | Constraint violation handling |
| **Type Coercion Safety** | No validation | `@MethodArgumentTypeMismatchException` handler | Type safety |

### 6.3 Authorization & Access Control

#### 6.3.1 PublicEndpointTests (`security/authorization/`)

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

#### 6.3.2 AdminBoundaryTests (`security/authorization/`)

Suite di **6 test** che verifica che solo **Admin** (e Gestore) possano registrare nuovo staff:

- `testRegisterStaff_WithClienteRole_ShouldReturn401` — Cliente bloccato
- `testRegisterStaff_WithAgenteRole_ShouldReturn401` — AgenteImmobiliare bloccato
- `testRegisterStaff_WithGestoreRole_ShouldReturn200` — **Gestore piò registrare staff**
- `testRegisterStaff_WithAdminRole_ShouldReturn200` — **Admin può registrare staff**
- `testRegisterStaff_WithoutAuthentication_ShouldReturn401` — Unauthenticated bloccato
- `testRegisterStaff_WithInvalidRole_ShouldThrowException` — Role sconosciuto bloccato

**Outcome**: Implementazione di `TokenUtils.checkIfAdminOrGestore()` in `AuthController.registraGestoreOrAgente()`.

#### 6.3.3 UtenteAgenziaBoundaryTests (`security/authorization/`)

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

#### 6.3.4 Data Isolation Tests (`security/dataisolation/`)

Suite di **3 categorie di test** che verificano che gli endpoint "riepilogo UtenteAgenzia" siano **accessibili solo a UtenteAgenzia**.

##### ImmobileOwnershipTests

Suite di **5 test** che verifica che:
- Agente vede **solo i suoi immobili** nella lista personale
- Cliente **non può** accedere a `/immobile/personali` (403)
- Gestore e Admin **possono** accedere
- Ricerca pubblica (`/immobile/cerca`) è accessibile a tutti

**Outcome**: `checkIfUtenteAgenzia()` blocca Cliente da accesso

##### OffertaPrivacyTests

Suite di **7 test** che verifica la separazione dei dati tra offerte di Cliente e UtenteAgenzia:

- `testRiepilogoOfferteCliente_WithClienteRole_ShouldReturn200` — Cliente vede **solo le sue offerte**
- `testRiepilogoOfferteUtenteAgenzia_WithClienteRole_ShouldReturn403` — Cliente **bloccato** da riepilogo agenzia

**Outcome**: Implementazione di `TokenUtils.checkIfUtenteAgenzia()` in endpoint `/offerta/riepilogoUtenteAgenzia`

**Nota di Sicurezza**: Precedentemente questo endpoint era **accessibile anche ai Cliente**, permettendo la fuga di dati riservati. **Fixato con aggiunta della guardia di autorizzazione**.

- `testRiepilogoOfferteUtenteAgenzia_WithAdminRole_ShouldReturn200` — Admin accede
- `testRiepilogoOfferteUtenteAgenzia_WithGestoreRole_ShouldReturn200` — Gestore accede
- `testRiepilogoOfferteUtenteAgenzia_WithAgenteRole_ShouldReturn200` — Agente accede
- `testRiepilogoOfferteUtenteAgenzia_WithoutAuthentication_ShouldReturn401` — Unauthenticated bloccato

##### VisitaPrivacyTests

Suite di **6 test** che verifica la separazione dei dati tra visite di Cliente e UtenteAgenzia:

- `testRiepilogoVisiteCliente_WithClienteRole_ShouldReturn200` — Cliente vede **solo le sue visite**
- `testRiepilogoVisiteUtenteAgenzia_WithClienteRole_ShouldReturn403` — Cliente **bloccato** da riepilogo agenzia
- `testRiepilogoVisiteUtenteAgenzia_WithAdminRole_ShouldReturn200` — Admin accede
- `testRiepilogoVisiteUtenteAgenzia_WithGestoreRole_ShouldReturn200` — Gestore accede
- `testRiepilogoVisiteUtenteAgenzia_WithAgenteRole_ShouldReturn200` — Agente accede
- `testRiepilogoVisiteUtenteAgenzia_WithoutAuthentication_ShouldReturn401` — Unauthenticated bloccato

**Outcome**: Implementazione di `TokenUtils.checkIfUtenteAgenzia()` in `/visita/riepilogoUtenteAgenzia`

#### 6.3.5 BruteForceAndAccountLockoutTests (`security/authorization/`)

Suite di **9 test** che verifica la protezione contro brute force attacks e il meccanismo di account lockout secondo **OWASP WSTG-AUTHN-02**:

**Failed Login Attempt Tracking**
- `testFirstFailedLoginAttempt_ShouldBeTracked` — Primo tentativo fallito registrato
- `testMultipleFailedLoginAttempts_ShouldBeCumulative` — Tentativi cumulativi tracciati per 3+ fallimenti

**Account Lockout Trigger**
- `testFifthFailedLoginAttempt_ShouldLockAccount` — Account bloccato dopo 5 tentativi falliti
- `testLockedAccount_ShouldRejectAllLoginAttempts` — Account bloccato rifiuta login anche con password corretta

**Lockout Duration & Auto-Unlock**
- `testAccountLockout_DurationShouldBeFifteenMinutesOrMore` — Lockout minimo 15 minuti (OWASP standard)
- `testAccountLockout_AutomaticUnlockAfterTimeout` — Account automaticamente sbloccato dopo scadenza lockout

**Counter Reset on Success**
- `testSuccessfulLogin_ShouldResetFailedAttemptCounter` — Login riuscito resetta counter a 0 dopo N fallimenti

**Account Enumeration Prevention**
- `testLockedAccount_MessageShouldNotRevealAccountExistence` — Messaggio errore non rivela se account è bloccato o inesistente (generic "Email o password non corrette")

**User Notification**
- `testAccountLockout_ShouldNotifyUserViaEmail` — Utente notificato via email quando account viene bloccato

**Implementazione**:

Model `Utente`:
```java
private int failedLoginAttempts;      // Counter di tentativi falliti
private Instant lockedUntil;          // Timestamp quando sarà sbloccato

public boolean isLocked() {
    return lockedUntil != null && Instant.now().isBefore(lockedUntil);
}
```

DAO `UtentePostgres`:
```java
// Nuovo metodo specifico per UPDATE login attempts
public void updateLoginAttempts(Utente u) { /* ... */ }
```

Service `AuthService`:
```java
public String login(String email, String password) {
    // Check Lockout
    // Wrong password -> increment counter
    // Counter >= 5 -> lock account (lockedUntil = now + 15 min)
    // Successful login -> reset counter (failedLoginAttempts = 0, lockedUntil = null
}
```

**Outcome**:
- Protezione contro brute force attacks automatica
- Account lockout dopo 5 tentativi falliti per 15 minuti
- Messaging generico per prevenire user enumeration 
- Email notification al lockout

#### 6.3.6 Miglioramenti Implementati

| Area                       | Prima                                          | Dopo                                                                  | Impact                    |
|----------------------------|------------------------------------------------|-----------------------------------------------------------------------|---------------------------|
| **RBAC**                   | Nessun controllo di ruolo                      | `checkIfAdmin()`, `checkIfAdminOrGestore()`, `checkIfUtenteAgenzia()` | Authorization enforcement |
| **Public vs Protected**    | Incoerente                                     | Esplicitamente definito in `SecurityConfig`                           | Clear security model      |
| **Endpoint Guarding**      | Assente                                        | Guardie in ogni endpoint sensibile                                    | Access control            |
| **Data Isolation**         | Cliente poteva accedere riepilogoUtenteAgenzia | Bloccato con `checkIfUtenteAgenzia()`                                 | Privacy protection        |
| **Brute Force Protection** | Nessuna                                        | 5 tentativi max + 15 min lockout                                      | Attack prevention         |
| **Account Lockout**        | Nessun lock-out                                | Auto-lock + notification                                              | Credential protection     |

### 6.4 Data Protection

Suite di **47 test** che verifica token JWT integrity, password hashing, e validazione crittografica.

#### 6.4.1 JwtServiceSecurityTests (`security/auth/`)

Suite di **19 test** che verifica il ciclo di vita completo dei token JWT:

**Token Generation & Claims**
- `testTokenGeneration_ShouldCreateValidToken` — JWT generato con claims corretti
- `testTokenGeneration_ShouldIncludeAllRequiredClaims` — Token contiene `sub`, `role`, `email`

**Token Validation & Expiration**
- `testTokenValidation_ValidTokenShouldBeAccepted` — Token valido decodificato correttamente
- `testTokenExpiration_ShouldRejectExpiredToken` — Token scaduto rifiutato con `UnauthorizedException`
- `testTokenClaimsIntegrity_ExpiredTokenClaimsShouldNotBeExtractable` — Impossibile estrarre claims da token scaduto

**Security Boundaries**
- `testTokenTampering_ShouldRejectTamperedToken` — Firma modificata causa rigetto
- `testTokenSignatureVerification_DifferentSecretShouldRejectToken` — Secret diverso causa rigetto
- `testMalformedToken_ShouldRejectInvalidFormat` — Formato invalido causa `UnauthorizedException`

**Claim Extraction**
- `testSubjectExtraction_ShouldExtractCorrectSubject` — Estrazione corretta di `sub` claim
- `testRoleExtraction_ShouldExtractCorrectRole` — Estrazione corretta di `role` claim

**Role Stealing Integration** (`RoleStealingIntegrationTests`)
- `testRoleStealing_ClientForgesAdminRole_ShouldReturn401` — JWT con `role=Admin` forgiato (firma invalida) rifiutato con HTTP 401

**Outcome**: Implementazione di `JwtService` con:
- Generazione token con `JwtEncoder` e claims `sub`, `role`, `email`
- Decodifica sicura con `JwtDecoder`
- Exception handling: `JwtException` → `UnauthorizedException`
- Metodi di estrazione claim: `extractSubject()`, `extractRole()`

**Nota di Sicurezza**: Token signature è validata via Spring Security RSA/HMAC, impedendo tampering payload.

#### 6.4.2 AuthServiceSecurityTests (`security/auth/`)

Suite di **8 test** che verifica password hashing con BCrypt:

**Password Hashing Security**
- `testPasswordHashingNotPlaintext` — Password hash **non** è plaintext
- `testPasswordHashingUniqueness` — Stessa password genera **diversi hash** (salt diverso)
- `testRegistrationHashesPasswordBeforeSave` — Password hashata prima di persistere

**Login Security**
- `testLoginWithWrongPassword_ShouldThrowUnauthorizedException` — Password errata → `UnauthorizedException`
- `testLoginWithWrongPasswordDoesNotRevealEmailExistence` — Error message **non rivela** se email esiste (Information Hiding)
- `testPasswordMatching_WithCorrectPassword_ShouldPass` — Password corretta verifica e ritorna JWT

**Timing Attack Resistance**
- `testTimingAttackResistance` — BCrypt mantiene tempo consistente per password errata

**Nota di Sicurezza**:
- BCrypt è resistant a timing attacks per natura
- Error messages **non differenziano** tra email inesistente e password errata

#### 6.4.3 PasswordPolicySecurityTests (`security/auth/`)

Suite di **15 test** che verifica la conformità della password policy secondo **OWASP WSTG-AUTHN-03**:

**Minimum Length Requirements (OWASP: min 8 characters)**
- `testPasswordMinimumLength_SevenCharacters_ShouldReject` — Password con 7 caratteri rifiutata
- `testPasswordMinimumLength_EightCharacters_ShouldAccept` — Password con 8 caratteri accettata (soglia minima)
- `testPasswordMaximumLength_ExceeedsLimit_ShouldReject` — Password oltre 255 caratteri rifiutata

**Password Complexity Requirements (Uppercase, Lowercase, Digits, Special Chars)**
- `testPasswordComplexity_MissingUppercase_ShouldReject` (parametrizzato x3) — Password senza maiuscola rifiutata
- `testPasswordComplexity_MissingLowercase_ShouldReject` (parametrizzato x3) — Password senza minuscola rifiutata
- `testPasswordComplexity_MissingNumber_ShouldReject` (parametrizzato x3) — Password senza numero rifiutata
- `testPasswordComplexity_MissingSpecialChar_ShouldReject` (parametrizzato x3) — Password senza carattere speciale rifiutata
- `testPasswordComplexity_ValidPassword_ShouldAccept` — Password valida (maiuscola + minuscola + numero + speciale) accettata

**Password Pattern Validation (No Sequential/Repeating Characters)**
- `testPasswordPattern_SequentialCharacters_ShouldReject` (parametrizzato x3) — Password con caratteri sequenziali rifiutata

**Email Parts Validation**
- `testPassword_ContainsEmailParts_ShouldReject` (parametrizzato x2) — Password contenente parti dell'email rifiutata

**Whitespace Handling**
- `testPassword_ContainsSpaces_ShouldReject` — Password con spazi rifiutata
- `testPassword_ContainsLeadingTrailingSpaces_ShouldReject` — Password con spazi iniziali/finali rifiutata

**Password Encoding Verification**
- `testPasswordEncoding_ShouldUseBCrypt` — Password encodata con BCrypt durante registrazione

**Nota di Sicurezza**:
- Validazione pattern regex: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,255}$`
- Caratteri speciali ammessi: `@$!%*?&`
- Email parts check previene password che contengono parti dell'indirizzo email (username o dominio)

**Outcome**: Implementazione di validazione `@Pattern` in `RegistrazioneRequest.password`:
```java
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,255}$",
    message = "La password deve contenere: maiuscola, minuscola, numero e carattere speciale (@$!%*?&), minimo 8 caratteri"
)
private String password;
```

#### 6.4.4 Miglioramenti Implementati

| Area                      | Prima                        | Dopo | Impact |
|---------------------------|------------------------------|------|--------|
| **JWT Generation**        | Assente                      | Token con `sub`, `role`, `email` claims | Token integrity |
| **JWT Validation**        | Nessuna                      | Spring Security + `JwtDecoder` | Attack prevention |
| **Password Storage**      | Plaintext risk               | BCrypt con salt random | Credential protection |
| **Password Verification** | Nessuna                      | BCrypt `matches()` timing-safe | Timing attack resistance |
| **Error Messages**        | Specifici per email/password | Generic "Email o password non corrette" | User enumeration prevention |
| **Token Expiration**      | Non implementato             | 1 ora (3600s) con `expiresAt` | Session timeout |
| **Password Strength**     | Nessuna validazione          | 8+ chars, uppercase, lowercase, digit, special char | OWASP WSTG-AUTHN-03 compliance |
| **Password Patterns**      | Nessun controllo             | No sequential/repeating, no email parts | Common pattern prevention |

### 6.5 Business Logic Security Tests

Suite di **125** test che verifica la sicurezza della logica di business, in particolare la validazione delle transizioni di stato, l'handling sicuro delle eccezioni e il rifiuto di input malevoli a livello di service.

#### 6.5.1 State Transition Validation Tests (`security/business/`)

##### Offerta State Machine

Suite di **6 test** in `OffertaStateTransitionTests`:

- `testInSospeso_ToAccettata_ShouldBeValid` — Transizione permessa ✓
- `testInSospeso_ToRifiutata_ShouldBeValid` — Transizione permessa ✓
- `testInSospeso_ToInSospeso_ShouldBeInvalid` — Transizione a stesso stato rifiutata
- `testAccettata_ToRifiutata_ShouldBeInvalid` — Stato terminale non può transizionare
- `testAccettata_ToAccettata_ShouldBeInvalid` — Stato terminale non può transizionare
- `testRifiutata_ToAccettata_ShouldBeInvalid` — Stato terminale non può transizionare

**Outcome**: Implementazione di `OffertaService.isTransazioneValida()` che valida transizioni secondo la state machine.

##### Visita State Machine

Suite di **6 test** in `VisitaStateTransitionTests`:

- `testInSospeso_ToConfermata_ShouldBeValid` — Transizione permessa ✓
- `testInSospeso_ToRifiutata_ShouldBeValid` — Transizione permessa ✓
- `testInSospeso_ToInSospeso_ShouldBeInvalid` — Transizione a stesso stato rifiutata
- `testConfermata_ToRifiutata_ShouldBeInvalid` — Stato terminale non può transizionare
- `testConfermata_ToConfermata_ShouldBeInvalid` — Stato terminale non può transizionare
- `testRifiutata_ToConfermata_ShouldBeInvalid` — Stato terminale non può transizionare

**Outcome**: Implementazione di `VisitaService.isTransizioneValidaVisita()` che valida transizioni secondo la state machine.

#### 6.5.2 Exception Handling Tests (`security/business/`)

##### Offerta Exception Handling Tests

Suite di **5 test** in `OffertaExceptionHandlingTests`:

**Authorization Checks**
- `testClienteModificaOffertaAltrui_ShouldThrowUnauthorizedException` — Client1 tenta di modificare offerta di Client2 → `UnauthorizedException` con messaggio generico "Utente non autorizzato"
- `testAgenteModificaOffertaCollega_ShouldThrowUnauthorizedException` — Agente1 tenta di modificare offerta su immobile di Agente2 → `UnauthorizedException`

**Resource Not Found**
- `testClienteModificaOffertaInesistente_ShouldThrowNotFoundException` — Offerta inesistente → `NotFoundException` con messaggio "Offerta non trovato"

**Exception Wrapping**
- `testDAOThrowsException_ShouldBeWrappedInGenericException` — Se DAO lancia `RuntimeException` → `InternalServerErrorException` con messaggio generico "Errore interno del server" (stack trace non incluso)

**Guard Ordering**
- `testOwnershipCheckBeforeStateValidation_ShouldThrowUnauthorizedException` — Se Client1 tenta di modificare offerta Client2 con stato invalido, riceve `UnauthorizedException` (non `BadRequestException`). Questo garantisce che l'attaccante non possa scoprire lo stato interno.

**Outcome**:
- Messaggi di errore generici che non rivelano dettagli
- Stack trace non propagato al client
- Ownership check eseguito **prima** della validazione di stato

##### Visita Exception Handling Tests

Suite di **5 test** in `VisitaExceptionHandlingTests` con stessa struttura di `OffertaExceptionHandlingTests`:

- `testClienteModificaVisitaAltrui_ShouldThrowUnauthorizedException`
- `testAgenteModificaVisitaCollega_ShouldThrowUnauthorizedException`
- `testClienteModificaVisitaInesistente_ShouldThrowNotFoundException`
- `testDAOThrowsException_ShouldBeWrappedInGenericException`
- `testOwnershipCheckBeforeStateValidation_ShouldThrowUnauthorizedException`

##### Immobile Exception Handling Tests

Suite di **5 test** in `ImmobileExceptionHandlingTests`:

**Data Access Error Handling**
- `testCercaImmobili_DAOThrowsDataAccessException_ShouldWrapWithoutLeaking` — Se DAO lancia `DataAccessException` → `InternalServerErrorException` generico
- `testImmobiliPersonali_DAOThrowsDataAccessException_ShouldWrapWithoutLeaking` — Stessa protezione per endpoint personali

**Geographic Service Validation**
- `testCreaImmobile_GeoServiceReturnsNull_ShouldThrowBadRequest` — Se GeoData service non trova coordinate → `BadRequestException` (input non valido)

**Constraint Violation Handling**
- `testCreaImmobile_DAOConstraintViolation_ShouldThrowConflict` — Se DAO lancia `DataIntegrityViolationException` → `ConflictException` (non 500 error)

**Database Error Wrapping**
- `testCreaImmobile_DAOThrowsDataAccessException_ShouldWrapWithoutLeaking` — Se DAO lancia `DataAccessException` → `InternalServerErrorException` generico

**Outcome**: Errori database wrappati in eccezioni di business, stack trace non leakato.

##### Geoapify GeoData Exception Handling Tests

Suite di **5 test** in `GeoapifyGeoDataExceptionHandlingTests`:

**API Response Validation**
- `testOttieniCoordinate_InvalidResponse_ShouldThrowBadRequest` — Se API non ritorna "features" → `BadRequestException`
- `testOttieniCoordinate_EmptyFeatures_ShouldThrowBadRequest` — Se "features" array è vuoto → `BadRequestException`
- `testOttieniConteggioPuntiInteresse_NoFeatures_ShouldThrowNotFound` — Se API non trova features per categoria → `NotFoundException`

**Network Error Handling**
- `testOttieniCoordinate_APIUnreachable_ShouldWrapWithoutLeaking` — Se API esterna non raggiungibile (RestClientException) → `InternalServerErrorException` generico (non rivela dettagli di rete)

**Input Validation**
- `testOttieniConteggioPuntiInteresse_UnsupportedCategory_ShouldThrowBadRequest` — Se categoria non supportata → `BadRequestException`

**Outcome**: Errori di API esterne non leakano dettagli di rete, messaggi generici per client.

##### Open Meteo Weather Exception Handling Tests

Suite di **10 test** in `OpenMeteoExceptionHandlingTests`:

**API Response Validation**
- `testOttieniPrevisioni_InvalidResponse_ShouldThrowInternalError` — Se API non ritorna "daily" → `InternalServerErrorException`
- `testOttieniPrevisioni_DateNotFound_ShouldThrowBadRequest` — Se data non trovata nei dati → `BadRequestException`
- `testOttieniPrevisioni_DailyIsNull_ShouldHandleGracefully` — Se "daily" è null → `InternalServerErrorException`
- `testOttieniPrevisioni_NullResponse_ShouldThrowInternalError` — API restituisce null → `InternalServerErrorException(EXTERNAL_SERVICE_ERROR)` (WSTG-ERRH-01)

**Network Error Handling**
- `testOttieniPrevisioni_APIUnreachable_ShouldWrapWithoutLeaking` — Se API non raggiungibile → `InternalServerErrorException` generico

**Null entries in `daily` — NPE handling (WSTG-ERRH-01)**
- `testOttieniPrevisioni_TimeListIsNull_ShouldWrapGeneric` — `daily.time` è null → `NullPointerException` wrappata in `InternalServerErrorException`
- `testOttieniPrevisioni_TemperatureMaxIsNull_ShouldWrapGeneric` — `daily.temperature_2m_max` è null → NPE wrappata senza rivelare stack

**Array length mismatch (WSTG-ERRH-01)**
- `testOttieniPrevisioni_MismatchedArrayLengths_ShouldWrapGeneric` — array temperature più corti della lista date → `IndexOutOfBoundsException` wrappata in `InternalServerErrorException`

**Type confusion — risposta API con tipo errato (WSTG-INPV-11)**
- `testOttieniPrevisioni_DailyIsWrongType_ShouldWrapGeneric` — `daily` è una String anziché Map → `ClassCastException` wrappata senza rivelare dettagli di tipo

**Defensive default (WSTG-ERRH-01)**
- `testOttieniPrevisioni_UnknownWeatherCode_ShouldReturnDefaultDescription` — codice meteo sconosciuto (999) → `descriviTempo()` ritorna `"Non disponibile"` senza eccezioni

**Outcome**: Tutti i possibili guasti dell'API esterna (null, tipo errato, array mismatch, NPE) vengono wrappati senza propagare dettagli interni al client.

##### AuthService Exception Handling Tests

Suite di **5 test** in `AuthServiceExceptionHandlingTests`:

**User Enumeration Prevention**
- `testLogin_EmailNotFound_ShouldThrowNotFound` — Email inesistente → `NotFoundException` con messaggio generico "Email o password non corrette"
- `testLogin_IncorrectPassword_ShouldThrowNotFound` — Password errata → Stesso `NotFoundException` (attaccante non può distinguere tra email inesistente e password errata)

**Registration Conflict Handling**
- `testRegistraCliente_EmailAlreadyExists_ShouldThrowConflict` — Email già usata → `ConflictException`

**Database Error Wrapping**
- `testRegistraCliente_DAOThrowsDataAccessException_ShouldWrapWithoutLeaking` — Se DAO lancia exception → `InternalServerErrorException` generico
- `testLogin_DAOThrowsDataAccessException_ShouldWrapAsNotFound` — Errori database durante login wrappati come `NotFoundException` per coerenza

**Outcome**: Impossibile fare user enumeration (email scanning). Messaggi di errore identici per email inesistente e password errata.

##### JWT Service Exception Handling Tests

Suite di **5 test** in `JwtServiceExceptionHandlingTests`:

**Token Validation Consistency**
- `testValidateAndDecodeToken_MalformedToken_ShouldThrowUnauthorized` — Token malformato → `UnauthorizedException` con messaggio "Token non valido o scaduto"
- `testValidateAndDecodeToken_ExpiredToken_ShouldThrowUnauthorized` — Token scaduto → **Stesso messaggio** (attaccante non può distinguere tra malformato e scaduto)
- `testValidateAndDecodeToken_InvalidSignature_ShouldThrowUnauthorized` — Firma modificata → **Stesso messaggio**

**Claim Extraction**
- `testExtractSubject_InvalidToken_ShouldThrowUnauthorized` — Se token non valido durante estrazione claims → `UnauthorizedException`
- `testExtractRole_InvalidToken_ShouldThrowUnauthorized` — Stessa protezione per role extraction

**Outcome**: Impossibile determinare il motivo della validazione fallita dal messaggio di errore. Token tampering rilevato e rifiutato senza rivelare il tipo di errore.

#### 6.5.3 ImmobileServiceInputValidationTests (`security/business/`)

Suite di **13 test** — WSTG-INPV-01, WSTG-BUSL-07, WSTG-ERRH-01

Verifica che `ImmobileService` applichi i controlli di input **prima di raggiungere il layer DAO o i servizi esterni**, coprendo i rami non testati di `cercaImmobili`, `creaImmobile` e `immobiliPersonali`.

**Paginazione — page size fuori limite (WSTG-BUSL-07)**
- `testCercaImmobili_PageSizeExceedsLimit_ShouldThrowBadRequest` — `size > 100` → `INVALID_PAGE_SIZE` (previene dump dell'intero dataset in una chiamata)

**Filtri prezzo — valori negativi e range invertito (WSTG-BUSL-07)**
- `testCercaImmobili_NegativePrezzoMin_ShouldThrowBadRequest` — `prezzoMin < 0` → `INVALID_PRICE`
- `testCercaImmobili_NegativePrezzoMax_ShouldThrowBadRequest` — `prezzoMax < 0` → `INVALID_PRICE`
- `testCercaImmobili_InvertedPriceRange_ShouldThrowBadRequest` — `prezzoMin > prezzoMax` → `INVALID_PRICE_RANGE`

**Filtri dimensione e bagni — valori negativi (WSTG-BUSL-07)**
- `testCercaImmobili_NegativeDimensione_ShouldThrowBadRequest` — `dimensione < 0` → `INVALID_DIMENSION`
- `testCercaImmobili_NegativeNBagni_ShouldThrowBadRequest` — `nBagni < 0` → `INVALID_BATHROOMS`

**Identity field tampering — null/blank uidResponsabile (WSTG-INPV-01)**
- `testCreaImmobile_NullUidResponsabile_ShouldThrowBadRequest` — null → `INVALID_RESPONSABILE` prima di qualsiasi chiamata esterna
- `testCreaImmobile_BlankUidResponsabile_ShouldThrowBadRequest` — whitespace → `INVALID_RESPONSABILE`
- `testImmobiliPersonali_NullUidResponsabile_ShouldThrowBadRequest` — null → `INVALID_RESPONSABILE` prima del DAO
- `testImmobiliPersonali_BlankUidResponsabile_ShouldThrowBadRequest` — whitespace → `INVALID_RESPONSABILE`

**Coordinate map integrity — risposta geo parziale (WSTG-INPV-01)**
- `testCreaImmobile_CoordinateMissingLatitudine_ShouldThrowBadRequest` — mappa senza chiave `"latitudine"` → `INVALID_ADDRESS`
- `testCreaImmobile_CoordinateMissingLongitudine_ShouldThrowBadRequest` — mappa senza chiave `"longitudine"` → `INVALID_ADDRESS`

**DAO write failure — error handling (WSTG-ERRH-01)**
- `testCreaImmobile_DaoReturnsFalse_ShouldThrowDatabaseError` — `immobileDao.creaImmobile()` → false → `DatabaseErrorException` senza leak stack

**Outcome**: I controlli di input in `ImmobileService` bloccano payload fuori range e risposte esterne incomplete prima di qualsiasi interazione con il database.

#### 6.5.4 OffertaServiceInputValidationTests (`security/business/`)

Suite di **17 test** — WSTG-INPV-01, WSTG-INPV-05, WSTG-BUSL-07, WSTG-ATHZ-02, WSTG-ERRH-01

Verifica che `OffertaService` applichi i controlli di input e le regole della macchina a stati **prima di raggiungere il layer DAO**, coprendo i rami di `aggiungiOfferta`, `aggiornaStatoOfferta`, `riepilogoOfferteCliente` e `riepilogoOfferteUtenteAgenzia`.

**Identity field tampering — null/blank UID guards (WSTG-INPV-01)**
- `testAggiungiOfferta_NullUidCliente_ShouldThrowBadRequest` — null uidCliente → `INVALID_CLIENT_ID` prima del DAO
- `testAggiungiOfferta_BlankUidCliente_ShouldThrowBadRequest` — whitespace uidCliente → `INVALID_CLIENT_ID`
- `testAggiornaStatoOfferta_NullUidUtente_ShouldThrowBadRequest` — null uidUtente → `INVALID_USER_ID`
- `testAggiornaStatoOfferta_BlankUidUtente_ShouldThrowBadRequest` — whitespace uidUtente → `INVALID_USER_ID`
- `testRiepilogoOfferteCliente_NullId_ShouldThrowBadRequest` — null idCliente → `INVALID_CLIENT_ID`
- `testRiepilogoOfferteCliente_BlankId_ShouldThrowBadRequest` — whitespace idCliente → `INVALID_CLIENT_ID`
- `testRiepilogoOfferteUtenteAgenzia_NullId_ShouldThrowBadRequest` — null idAgente → `INVALID_USER_ID`
- `testRiepilogoOfferteUtenteAgenzia_BlankId_ShouldThrowBadRequest` — whitespace idAgente → `INVALID_USER_ID`

**Injection nel campo stato — enum allowlist (WSTG-INPV-05)**
- `testAggiornaStatoOfferta_SqlInjectionInStato_ShouldThrowBadRequest` — `UPDATE … WHERE '1'='1` come stato → `INVALID_STATUS` (non raggiunge il DAO)
- `testAggiornaStatoOfferta_InvalidStatoString_ShouldThrowBadRequest` — stringa arbitraria → `INVALID_STATUS`

**State machine bypass — transizioni non consentite (WSTG-BUSL-07)**
- `testAggiornaStatoOfferta_RifiutataToAccettata_ShouldThrowBadRequest` — RIFIUTATA → ACCETTATA → `INVALID_OFFERTA_STATUS` (stato terminale non può riaprirsi)
- `testAggiornaStatoOfferta_AccettataToRifiutata_ShouldThrowBadRequest` — ACCETTATA → RIFIUTATA → `INVALID_OFFERTA_STATUS` (stato terminale non può transizionare)

**Horizontal privilege escalation (WSTG-ATHZ-02)**
- `testAggiornaStatoOfferta_ClienteUpdatesOtherClientOffer_ShouldThrowUnauthorized` — attacker-uid tenta di modificare l'offerta di victim-uid → `UNAUTHORIZED`

**Error handling — DAO failures (WSTG-ERRH-01)**
- `testAggiornaStatoOfferta_DaoUpdateReturnsFalse_ShouldThrowDatabaseError` — DAO update → false → `DatabaseErrorException`
- `testAggiornaStatoOfferta_NonExistentOfferta_ShouldThrowNotFoundException` — `EmptyResultDataAccessException` → `NotFoundException(OFFERTA_NOT_FOUND)`
- `testAggiungiOfferta_ImmobileFKViolation_ShouldThrowNotFound` — FK su `idImmobile` → `NotFoundException(IMMOBILE_NOT_FOUND)`
- `testAggiungiOfferta_ClienteFKViolation_ShouldThrowNotFound` — FK su `idCliente` → `NotFoundException(CLIENT_NOT_FOUND)`

**Outcome**: I controlli di input in `OffertaService` bloccano UID tamperati, stati iniettati e transizioni illegali prima di qualsiasi interazione con il database. Le eccezioni DAO vengono sempre wrapped in eccezioni di business.

#### 6.5.5 VisitaServiceInputValidationTests (`security/business/`)

Suite di **17 test** — WSTG-INPV-01, WSTG-INPV-05, WSTG-BUSL-07, WSTG-ERRH-01

Verifica che `VisitaService` applichi i controlli di input **prima di raggiungere il layer DAO**, coprendo i rami di `prenotaVisita`, `aggiornaStatoVisita`, `riepilogoVisiteCliente` e `riepilogoVisiteUtenteAgenzia` non testati in precedenza.

**Identity field tampering — null/blank UID (WSTG-INPV-01)**
- `testPrenotaVisita_NullUidCliente_ShouldThrowBadRequest` — null uidCliente → `INVALID_CLIENT_ID` prima del DAO
- `testPrenotaVisita_BlankUidCliente_ShouldThrowBadRequest` — whitespace uidCliente → `INVALID_CLIENT_ID`
- `testRiepilogoVisiteCliente_NullId_ShouldThrowBadRequest` — null idCliente → `INVALID_CLIENT_ID`
- `testRiepilogoVisiteCliente_BlankId_ShouldThrowBadRequest` — whitespace idCliente → `INVALID_CLIENT_ID`
- `testRiepilogoVisiteUtenteAgenzia_NullId_ShouldThrowBadRequest` — null idAgente → `INVALID_USER_ID`
- `testRiepilogoVisiteUtenteAgenzia_BlankId_ShouldThrowBadRequest` — whitespace idAgente → `INVALID_USER_ID`
- `testAggiornaStatoVisita_NullUidUtente_ShouldThrowBadRequest` — null uidUtente → `INVALID_USER_ID`
- `testAggiornaStatoVisita_BlankUidUtente_ShouldThrowBadRequest` — whitespace uidUtente → `INVALID_USER_ID`

**Business logic bypass — out-of-hours booking (WSTG-BUSL-07)**
- `testPrenotaVisita_TimeBeforeWorkHours_ShouldThrowBadRequest` — visita alle 07:59 → `INVALID_VISIT_TIME`
- `testPrenotaVisita_TimeAfterWorkHours_ShouldThrowBadRequest` — visita alle 18:01 → `INVALID_VISIT_TIME`

**Referential integrity attacks — FK forgiata (WSTG-INPV-01)**
- `testPrenotaVisita_ImmobileFKViolation_ShouldThrowNotFoundException` — `DataIntegrityViolationException` su `immobile` → `NotFoundException(IMMOBILE_NOT_FOUND)` senza leak DB
- `testPrenotaVisita_ClienteFKViolation_ShouldThrowNotFoundException` — `DataIntegrityViolationException` su `idcliente` → `NotFoundException(CLIENT_NOT_FOUND)`
- `testPrenotaVisita_DaoReturnsFalse_ShouldThrowDatabaseError` — DAO restituisce false → `DatabaseErrorException` senza leak stack

**Injection in campo stato (WSTG-INPV-05)**
- `testAggiornaStatoVisita_SqlInjectionInStato_ShouldThrowBadRequest` — `'; DROP TABLE visita; --'` come stato → `INVALID_STATUS` (non raggiunge il DAO)
- `testAggiornaStatoVisita_ArbitraryStato_ShouldThrowBadRequest` — stringa arbitraria → `INVALID_STATUS`

**Error handling — DAO failures (WSTG-ERRH-01)**
- `testAggiornaStatoVisita_DaoUpdateReturnsFalse_ShouldThrowDatabaseError` — DAO update → false → `DatabaseErrorException`
- `testAggiornaStatoVisita_EmptyResultException_ShouldThrowNotFoundException` — `EmptyResultDataAccessException` → `NotFoundException(RESOURCE_NOT_FOUND)` senza leak stack

**Outcome**: I controlli di input in `VisitaService` bloccano payload malevoli prima di ogni interazione con il database. Le eccezioni DAO vengono sempre wrapped in eccezioni di business.

#### 6.5.6 MeteoServiceSecurityTests (`security/business/`)

Suite di **15 test** — WSTG-INPV-01, WSTG-INPV-05, WSTG-BUSL-07

Verifica che `MeteoService` validi e rifiuti input malevoli nelle coordinate e nella data prima di propagarli all'API esterna.

**Injection nel parametro data (WSTG-INPV-05)**
- `testOttieniPrevisioni_SqlInjectionInDate_ShouldThrowBadRequest` — `' OR '1'='1` → `INVALID_DATE_FORMAT`
- `testOttieniPrevisioni_UnionInjectionInDate_ShouldThrowBadRequest` — UNION injection → `INVALID_DATE_FORMAT`
- `testOttieniPrevisioni_XssInDate_ShouldThrowBadRequest` — `<script>alert(1)</script>` → `INVALID_DATE_FORMAT` (WSTG-INPV-01)
- `testOttieniPrevisioni_MalformedDate_ShouldThrowBadRequest` — stringa non ISO-8601 → `INVALID_DATE_FORMAT`

**Date range enforcement — date passate (WSTG-BUSL-07)**
- `testOttieniPrevisioni_PastDate_ShouldThrowBadRequest` — ieri → `INVALID_DATE_RANGE`
- `testOttieniPrevisioni_FarPastDate_ShouldThrowBadRequest` — 30 giorni fa → `INVALID_DATE_RANGE`

**Injection nelle coordinate (WSTG-INPV-05)**
- `testOttieniPrevisioni_SqlInjectionInLatitude_ShouldThrowBadRequest` — OR injection in latitudine → `INVALID_COORDINATES`
- `testOttieniPrevisioni_SqlInjectionInLongitude_ShouldThrowBadRequest` — DROP TABLE in longitudine → `INVALID_COORDINATES`
- `testOttieniPrevisioni_XssInLatitude_ShouldThrowBadRequest` — payload XSS → `INVALID_COORDINATES` (WSTG-INPV-01)
- `testOttieniPrevisioni_LongStringInLatitude_ShouldThrowBadRequest` — stringa da 10.000 caratteri → `INVALID_COORDINATES` (WSTG-INPV-13)
- `testOttieniPrevisioni_NullByteInLatitude_ShouldThrowBadRequest` — null byte `\u0000` → `INVALID_COORDINATES`

**Boundary validation delle coordinate (WSTG-INPV-01)**
- `testOttieniPrevisioni_LatitudeBelowMin_ShouldThrowBadRequest` — `-90.001` → `INVALID_LATITUDE`
- `testOttieniPrevisioni_LatitudeAboveMax_ShouldThrowBadRequest` — `90.001` → `INVALID_LATITUDE`
- `testOttieniPrevisioni_LongitudeBelowMin_ShouldThrowBadRequest` — `-180.001` → `INVALID_LONGITUDE`
- `testOttieniPrevisioni_LongitudeAboveMax_ShouldThrowBadRequest` — `180.001` → `INVALID_LONGITUDE`

**Bug risolto durante il testing**:

| Bug | File | Problema | Fix |
|-----|------|----------|-----|
| **Condizione date range invertita** | `MeteoService.java` | `if (dataRichiesta.isBefore(dataMassimaSupportata))` rifiutava le date valide (oggi → oggi+6) e accettava quelle oltre i 7 giorni | Corretta in `if (dataRichiesta.isBefore(dataAttuale) \|\| dataRichiesta.isAfter(dataMassimaSupportata))` |

**Outcome**: Qualsiasi payload non numerico nelle coordinate viene rifiutato prima di raggiungere l'API esterna. La validazione del range temporale è ora corretta.

#### 6.5.7 GeoDataExceptionHandlingTests (`security/business/`)

Suite di **5 test** — WSTG-INPV-01, WSTG-ERRH-01

Verifica le proprietà di sicurezza di `GeoDataService` come service layer, in particolare che le eccezioni provenienti dal layer DAO non vengano silenziate e che la concatenazione dell'indirizzo avvenga correttamente prima del passaggio al DAO.

**Exception propagation — DAO failures non silenziate (WSTG-ERRH-01)**
- `testOttieniCoordinate_DaoThrowsInternalError_ShouldPropagate` — `InternalServerErrorException(EXTERNAL_SERVICE_ERROR)` dal DAO propagata al caller senza modifiche
- `testOttieniCoordinate_DaoThrowsAddressNotFound_ShouldPropagate` — `BadRequestException(ADDRESS_NOT_FOUND)` dal DAO propagata con `ErrorCode` originale intatto
- `testOttieniConteggioPuntiInteresse_DaoThrowsInvalidCategory_ShouldPropagate` — `BadRequestException(INVALID_CATEGORY)` dal DAO propagata senza wrapping aggiuntivo
- `testOttieniConteggioPuntiInteresse_DaoThrowsInternalError_ShouldPropagate` — `InternalServerErrorException` dal DAO non viene silenziata dal service layer

**Address concatenation integrity (WSTG-INPV-01)**
- `testOttieniCoordinate_AddressConcatenation_ShouldForwardCorrectly` — `ottieniCoordinate("Via Roma 1", "Genova")` deve passare esattamente `"Via Roma 1, Genova"` al DAO senza manipolazione aggiuntiva o troncamento

**Outcome**: `GeoDataService` propaga fedelmente tutte le eccezioni del DAO senza sopprimerle, garantendo che gli errori delle API esterne risalgano correttamente al caller. L'address forwarding non altera i dati in input.

#### 6.5.8 EmailServiceSecurityTests (`security/business/`)

Suite di **5 test** — WSTG-ERRH-01, WSTG-INPV-13

Verifica le proprietà di sicurezza di `EmailService`, in particolare la propagazione delle eccezioni SMTP e il comportamento rispetto all'iniezione di header email tramite CRLF.

**MailException propagation — notifiche non silenziate (WSTG-ERRH-01)**
- `testInviaEmail_MailSendException_ShouldPropagate` — `MailSendException` da `JavaMailSender.send()` deve propagarsi: silenziare l'errore impedirebbe la corretta segnalazione del fallimento della notifica di account bloccato
- `testInviaEmail_RuntimeException_ShouldPropagate` — eccezioni runtime impreviste dall'infrastruttura mail non devono essere silenziate

**Email header injection — CRLF non sanitizzato (WSTG-INPV-13)**
- `testInviaEmail_CrlfInOggetto_IsForwardedUnmodified` — stringa `"Subject\r\nBCC: attacker@evil.com"` nel campo `oggetto` viene inoltrata a `JavaMailSender` senza sanitizzazione. Documenta che la validazione DEVE essere applicata a monte (controller/DTO) prima di invocare `EmailService`
- `testInviaEmail_CrlfInDestinatario_IsForwardedUnmodified` — CRLF nel campo `destinatario` inoltrato senza modifiche al sender. La prevenzione dell'header injection deve essere garantita dalla validazione `@Email` nel DTO

**Positive control**
- `testInviaEmail_ValidInputs_ShouldBeForwardedCorrectly` — verifica che destinatario, oggetto e testo vengano passati correttamente a `SimpleMailMessage.setTo/setSubject/setText`

**Outcome**: `EmailService` propaga le eccezioni SMTP senza sopprimerle. I test CRLF documentano esplicitamente che la sanitizzazione dell'header injection è responsabilità del layer di validazione upstream (controller o DTO), non del service.

#### 6.5.9 Miglioramenti Implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **State Transition** | Nessuna validazione transizioni | State machine validation in service (`isTransazioneValida`, `isTransizioneValidaVisita`) | Business logic integrity |
| **Exception Leakage - Stack Trace** | `"Errore interno: " + e.getMessage()` espone dettagli di database | `"Errore interno del server"` messaggio generico | Information disclosure prevention |
| **Guard Ordering** | Validazione di stato PRIMA di ownership check | Ownership check PRIMA di validazione stato | Information hiding on unauthorized access |
| **API External Errors** | Stack trace di API Geoapify/OpenMeteo leakato | Generic `InternalServerErrorException` | External service details protection |
| **Input Validation - Service Layer** | Assente su VisitaService e MeteoService | Null/blank guard, time boundary, FK violation mapping, injection rejection | Pre-DAO attack surface reduction |
| **Date Range Validation** | Condizione invertita in MeteoService | `isBefore(today) \|\| isAfter(today+7)` | Correct temporal boundary enforcement |

### 6.6 DAO Integration Security Tests

Suite di **49 test** che verifica la sicurezza del layer di persistenza testando direttamente le implementazioni DAO contro il database PostgreSQL reale. Tutti i test usano `@Transactional` con rollback automatico per garantire l'isolamento tra i test e i dati pre-seeded di `02_test_data.sql`.

#### 6.6.1 UtentePostgresDaoSecurityTests (`security/dao/`)

Suite di **9 test** — WSTG-INPV-05, WSTG-CRYP-04, WSTG-AUTHN-03

**SQL Injection (WSTG-INPV-05)**
- `testFindByEmail_WithOrInjection_ShouldNotReturnAnyUser` — OR injection su `findByEmail` → `EmptyResultDataAccessException` (la query parametrizzata tratta il payload come stringa letterale, non come SQL)
- `testFindByEmail_WithUnionInjection_ShouldNotLeakData` — UNION injection su `findByEmail` → `EmptyResultDataAccessException`
- `testFindByEmail_WithCommentInjection_ShouldNotBypassAuthentication` — comment injection (`admin@test.it'--`) → `EmptyResultDataAccessException` (il commento non tronca la clausola WHERE)
- `testFindByEmail_WithStackedQueryInjection_ShouldNotExecuteDDL` — stacked query (`x'; DROP TABLE utenti; --`) → `EmptyResultDataAccessException` (nessun DDL eseguito)
- `testFindEmailByUid_WithOrInjection_ShouldNotReturnAnyEmail` — OR injection su `findEmailByUid` → `EmptyResultDataAccessException`
- `testFindEmailByUid_WithUnionInjection_ShouldNotLeakData` — UNION injection su `findEmailByUid` → `EmptyResultDataAccessException`

**Integrità Dati Sensibili (WSTG-CRYP-04)**
- `testSave_ShouldStorePasswordHashUnchanged` — Il DAO persiste l'hash BCrypt **esattamente come fornito** dal service layer, senza alcuna alterazione

**Uniqueness constraint — prevenzione account hijacking (WSTG-INPV-01)**
- `testSave_WithDuplicateEmail_ShouldThrowDataIntegrityViolation` — Email duplicata → `DataIntegrityViolationException` (il vincolo UNIQUE impedisce la registrazione di due account con la stessa email)

**Brute force protection data integrity (WSTG-AUTHN-03)**
- `testUpdateLoginAttempts_ShouldPersistBruteForceProtectionData` — `updateLoginAttempts` persiste correttamente `failedLoginAttempts = 5` e `lockedUntil` nel futuro, garantendo l'integrità del meccanismo di lockout

**Outcome**: Le query parametrizzate di JdbcTemplate neutralizzano ogni tentativo di SQL injection. Il DAO non trasforma i dati sensibili in ingresso. I dati di protezione brute-force sono persistiti correttamente.

#### 6.6.2 ImmobilePostgresDaoSecurityTests (`security/dao/`)

Suite di **9 test** — WSTG-INPV-05, WSTG-ATHZ-02, WSTG-ERRH-01

**SQL Injection in `cercaImmobiliConFiltri` (WSTG-INPV-05)**
- `testCercaImmobili_WithOrInjectionInComune_ShouldReturnEmptyList` — OR injection nel filtro `comune` → lista vuota (non ritorna tutti gli immobili)
- `testCercaImmobili_WithUnionInjectionInComune_ShouldReturnEmptyList` — UNION injection nel filtro `comune` → lista vuota
- `testCercaImmobili_WithOrInjectionInTipologia_ShouldReturnEmptyList` — OR injection nel filtro `tipologia` → lista vuota (il payload non bypassa la clausola WHERE)
- `testCercaImmobili_WithUnionInjectionInTipologia_ShouldReturnEmptyList` — UNION injection in `tipologia` → lista vuota (parametrizzazione blocca l'estrazione dati)

**SQL Injection in `immobiliPersonali` (WSTG-INPV-05)**
- `testImmobiliPersonali_WithOrInjection_ShouldReturnEmptyList` — OR injection in `uidResponsabile` → lista vuota
- `testImmobiliPersonali_WithUnionInjection_ShouldReturnEmptyList` — UNION injection → lista vuota (nessun dato leakato da altri agenti)
- `testImmobiliPersonali_WithCommentInjection_ShouldReturnEmptyList` — comment injection (`uid'--`) → lista vuota (il commento non tronca la WHERE clause)

**Data Isolation (WSTG-ATHZ-02)**
- `testImmobiliPersonali_ShouldReturnOnlyOwnerProperties` — `immobiliPersonali("uid-agente-001")` restituisce **solo** gli immobili di agente-001, mai quelli di agente-002

**Error handling — risorsa inesistente (WSTG-ERRH-01)**
- `testGetImmobileById_NonExistentId_ShouldThrowEmptyResult` — ID inesistente → `EmptyResultDataAccessException` (non ritorna null, che maschererebbe l'errore nei layer superiori)

**Outcome**: SQL injection neutralizzato in tutti i filtri dinamici. Il filtro `WHERE idagente = ?` isola correttamente i dati per agente. `getImmobileById` solleva eccezione invece di restituire null per ID inesistenti.

#### 6.6.3 OffertaPostgresDaoSecurityTests (`security/dao/`)

Suite di **15 test** — WSTG-INPV-05, WSTG-ATHZ-02, WSTG-INPV-01, WSTG-BUSL-07, WSTG-ERRH-01

**SQL Injection in `riepilogoOfferteCliente` (WSTG-INPV-05)**
- `testRiepilogoOfferteCliente_WithOrInjection_ShouldReturnEmptyList` — OR injection in `idCliente` → lista vuota
- `testRiepilogoOfferteCliente_WithUnionInjection_ShouldReturnEmptyList` — UNION injection in `idCliente` → lista vuota (nessun dato leakato)
- `testRiepilogoOfferteCliente_WithCommentInjection_ShouldReturnEmptyList` — comment injection (`uid'--`) → lista vuota (il commento non tronca la WHERE)
- `testRiepilogoOfferteCliente_WithStackedQueryInjection_ShouldReturnEmptyList` — stacked query (`uid'; DROP TABLE offerta; --`) → lista vuota (nessun DDL eseguito)

**SQL Injection in `riepilogoOfferteUtenteAgenzia` (WSTG-INPV-05)**
- `testRiepilogoOfferteUtenteAgenzia_WithOrInjection_ShouldReturnEmptyList` — OR injection in `idAgente` → lista vuota
- `testRiepilogoOfferteUtenteAgenzia_WithUnionInjection_ShouldReturnEmptyList` — UNION injection in `idAgente` → lista vuota
- `testRiepilogoOfferteUtenteAgenzia_WithCommentInjection_ShouldReturnEmptyList` — comment injection (`uid'--`) → lista vuota (il commento non tronca la WHERE clause per agente)
- `testRiepilogoOfferteUtenteAgenzia_WithStackedQueryInjection_ShouldReturnEmptyList` — stacked query → lista vuota (nessun DDL eseguito per agente)

**Data Isolation (WSTG-ATHZ-02)**
- `testRiepilogoOfferteCliente_ShouldNotExposeOtherClientsOffers` — Offerte di cliente-001 non contengono mai offerte di cliente-002
- `testRiepilogoOfferteUtenteAgenzia_ShouldNotExposeOtherAgentsOffers` — Offerte visibili ad agente-001 riguardano esclusivamente i suoi immobili
- `testGetOffertaById_ValidId_ShouldReturnOnlyRequestedOffer` — `getOffertaById(1)` restituisce esattamente l'offerta con ID 1 e il cliente corretto, senza esporre offerte di altri utenti

**Referential integrity — FK enforcement (WSTG-INPV-01)**
- `testSalvaOfferta_NonExistentIdImmobile_ShouldThrowDataIntegrityViolation` — idImmobile inesistente → `DataIntegrityViolationException` (il vincolo FK impedisce inserimenti con riferimenti forgiati)
- `testSalvaOfferta_NonExistentIdCliente_ShouldThrowDataIntegrityViolation` — UID cliente inesistente → `DataIntegrityViolationException`

**State transition persistence at DB level (WSTG-BUSL-07)**
- `testAggiornaStatoOfferta_ShouldPersistStateTransitionCorrectly` — aggiorna offerta 1 da `IN_SOSPESO` → `ACCETTATA` e ri-legge per confermare che il nuovo stato è scritto correttamente nel DB

**Error handling — risorsa inesistente (WSTG-ERRH-01)**
- `testGetOffertaById_NonExistentId_ShouldThrowEmptyResult` — ID inesistente → `EmptyResultDataAccessException` (non ritorna null, che maschererebbe l'errore nei layer superiori)

**Outcome**: Tutti i vettori SQL injection (OR, UNION, comment, stacked query) neutralizzati per entrambi i metodi di listing. FK constraint verifica DB-level la validità dei riferimenti inseriti. La transizione di stato è persistita correttamente e verificabile con ri-lettura.

#### 6.6.4 VisitaPostgresDaoSecurityTests (`security/dao/`)

Suite di **12 test** — WSTG-INPV-05, WSTG-ATHZ-02, WSTG-INPV-01, WSTG-BUSL-07, WSTG-ERRH-01

**SQL Injection in `riepilogoVisiteCliente` (WSTG-INPV-05)**
- `testRiepilogoVisiteCliente_WithOrInjection_ShouldReturnEmptyList` — OR injection in `idCliente` → lista vuota
- `testRiepilogoVisiteCliente_WithUnionInjection_ShouldReturnEmptyList` — UNION injection in `idCliente` → lista vuota (nessun dato leakato)
- `testRiepilogoVisiteCliente_WithCommentInjection_ShouldReturnEmptyList` — comment injection (`uid'--`) → lista vuota (il commento non tronca la WHERE)
- `testRiepilogoVisiteCliente_WithStackedQueryInjection_ShouldReturnEmptyList` — stacked query (`uid'; DROP TABLE visita; --`) → lista vuota (nessun DDL eseguito)

**SQL Injection in `riepilogoVisiteUtenteAgenzia` (WSTG-INPV-05)**
- `testRiepilogoVisiteUtenteAgenzia_WithOrInjection_ShouldReturnEmptyList` — OR injection in `idAgente` → lista vuota
- `testRiepilogoVisiteUtenteAgenzia_WithUnionInjection_ShouldReturnEmptyList` — UNION injection in `idAgente` → lista vuota
- `testRiepilogoVisiteUtenteAgenzia_WithCommentInjection_ShouldReturnEmptyList` — comment injection (`uid'--`) → lista vuota

**Data Isolation (WSTG-ATHZ-02)**
- `testRiepilogoVisiteCliente_ShouldNotExposeOtherClientsVisits` — Visite di cliente-001 non contengono mai visite di cliente-002
- `testRiepilogoVisiteUtenteAgenzia_ShouldNotExposeOtherAgentsVisits` — Visite visibili ad agente-001 riguardano esclusivamente i suoi immobili

**Referential integrity — FK enforcement (WSTG-INPV-01)**
- `testSalva_NonExistentIdImmobile_ShouldThrowDataIntegrityViolation` — idImmobile inesistente → `DataIntegrityViolationException` (il vincolo FK impedisce inserimenti con riferimenti forgiati)

**State transition persistence at DB level (WSTG-BUSL-07)**
- `testAggiornaStato_ShouldPersistStateTransitionCorrectly` — aggiorna visita 2 da `IN_SOSPESO` → `CONFERMATA` e ri-legge per confermare che il nuovo stato è scritto correttamente nel DB

**Error handling — risorsa inesistente (WSTG-ERRH-01)**
- `testGetVisitaById_NonExistentId_ShouldThrowEmptyResult` — ID inesistente → `EmptyResultDataAccessException` (non ritorna null, che maschererebbe l'errore nei layer superiori)

**Outcome**: Tutti i vettori SQL injection neutralizzati per entrambi i metodi di listing. FK constraint verifica DB-level la validità degli immobili referenziati. La transizione di stato è persistita correttamente e verificabile con ri-lettura.

#### 6.6.5 UtenteAgenziaPostgresDaoSecurityTests (`security/dao/`)

Suite di **4 test** — WSTG-INPV-05, WSTG-ATHZ-02

**SQL Injection (WSTG-INPV-05)**
- `testGetIdAgenzia_WithOrInjection_ShouldThrowNotFoundException` — OR injection → `NotFoundException(ADMIN_NOT_FOUND)` (nessun utente corrisponde alla stringa iniettata)
- `testGetIdAgenzia_WithUnionInjection_ShouldThrowNotFoundException` — UNION injection → `NotFoundException(ADMIN_NOT_FOUND)`

**Authorization Bypass (WSTG-ATHZ-02)**
- `testGetIdAgenzia_WithNonAdminRole_ShouldThrowUnauthorizedException` — Utente con ruolo Cliente → `UnauthorizedException(INSUFFICIENT_PERMISSIONS)` prima di esporre qualsiasi dato agenzia
- `testGetIdAgenzia_WithNonExistentUid_ShouldThrowNotFoundException` — UID inesistente → `NotFoundException(ADMIN_NOT_FOUND)` senza leak di dettagli DB

**Outcome**: SQL injection neutralizzato. Il DAO verifica il ruolo Admin prima di restituire qualsiasi informazione sull'agenzia.

#### 6.6.6 Bug Risolti durante il Testing

Durante la scrittura dei test di integrazione DAO sono stati identificati e corretti tre bug che rendevano la protezione brute force e le operazioni di stato parzialmente non funzionanti:

| Bug | File | Problema | Fix |
|-----|------|----------|-----|
| **Colonne DB errate in UPDATE** | `UtentePostgres.java` | `updateLoginAttempts` usava `failed_login_attempts` e `locked_until` — colonne inesistenti nello schema | Corrette in `failedloginattempts` e `lockeduntil` |
| **SELECT incompleto in findByEmail** | `UtentePostgres.java` | `findByEmail` selezionava solo `uid, email, password, role` ma il RowMapper leggeva anche `failedloginattempts` e `lockeduntil` | Aggiunto `failedloginattempts, lockeduntil` alla clausola SELECT |
| **Enum mismatch con PostgreSQL** | `StatoOfferta.java`, `StatoVisita.java` | `IN_SOSPESO` produceva `"In sospeso"` ma il tipo ENUM PostgreSQL definisce `'In Sospeso'` — INSERT falliva silenziosamente in produzione | Corretta capitalizzazione in `"In Sospeso"` |
| **JDBC VARCHAR → ENUM cast mancante** | `OffertaPostgres.java`, `VisitaPostgres.java` | `salvaOfferta`, `aggiornaStatoOfferta`, `salva`, `aggiornaStato` passavano il valore `stato` come `character varying`; PostgreSQL rifiutava con `column "stato" is of type statoofferta/statovisita but expression is of type character varying` — scritture su offerta/visita impossibili in produzione | Aggiunto `CAST(? AS statoofferta)` / `CAST(? AS statovisita)` nelle query SQL; `aggiornaStatoOfferta` e `aggiornaStato` corretti anche per usare `getStatoString()` al posto di `toString()` |

### 6.7 Riepilogo Finale

#### Copertura dei Test

| Categoria | Numero di Test | Focus |
|-----------|---|--|
| **Input Validation & Boundary Testing** | 38 test | JSON parsing, type validation, payload size, SQL injection |
| **Authorization & Access Control** | 53 test | RBAC, endpoint protection, data isolation, brute force, account lockout |
| **Data Protection & Cryptography** | 42 test | JWT integrity, password hashing, token tampering, password policy |
| **Business Logic Security** | 125 test | State machine validation, exception handling, error message safety, service input validation |
| **DAO Integration Security** | 49 test | SQL injection at DB level, data isolation at query level, referential integrity, state persistence |
| **TOTALE** | **307 test** |

#### Conformità OWASP Testing Guide

| Sezione OWASP WSTG | Descrizione | Status | Test |
|---|---|---|---|
| **WSTG-AUTH-01** | Authentication Mechanisms Testing | ✓ Compliant | `JwtServiceSecurityTests` (19 test) |
| **WSTG-AUTHN-02** | Account Enumeration & Brute Force | ✓ Compliant | `BruteForceAndAccountLockoutTests` (9 test) |
| **WSTG-AUTHN-03** | Password Policy / Lockout Mechanism | ✓ Compliant | `PasswordPolicySecurityTests` (15 test), `UtentePostgresDaoSecurityTests` (1 test) |
| **WSTG-AUTHN-04** | Weak Authentication Mechanisms | ✓ Compliant | `AuthServiceSecurityTests` (8 test) |
| **WSTG-AUTHZ-01** | Directory Traversal/RBAC | ✓ Compliant | `AdminBoundaryTests` (6 test), `UtenteAgenziaBoundaryTests` (10 test) |
| **WSTG-AUTHZ-02** | Privilege Escalation / Data Isolation | ✓ Compliant | `ImmobileOwnershipTests` (7 test), `OffertaPrivacyTests` (7 test), `VisitaPrivacyTests` (6 test), `ImmobilePostgresDaoSecurityTests` (1 test), `OffertaPostgresDaoSecurityTests` (3 test), `VisitaPostgresDaoSecurityTests` (2 test), `UtenteAgenziaPostgresDaoSecurityTests` (2 test), `OffertaServiceInputValidationTests` (1 test) |
| **WSTG-IA-06** | Forced Browsing/Endpoint Discovery | ✓ Compliant | `PublicEndpointTests` (7 test) |
| **WSTG-INPV-01** | Input Validation — identity/boundary | ✓ Compliant | `ImmobileServiceInputValidationTests` (10 test), `OffertaServiceInputValidationTests` (9 test), `VisitaServiceInputValidationTests` (10 test), `MeteoServiceSecurityTests` (8 test), `GeoDataExceptionHandlingTests` (1 test), `UtentePostgresDaoSecurityTests` (1 test), `OffertaPostgresDaoSecurityTests` (2 test), `VisitaPostgresDaoSecurityTests` (1 test) |
| **WSTG-INPV-05** | SQL Injection | ✓ Compliant | `MalformedPayloadTests` (13 test), `LoginRequestValidationTests` (10 test), `OffertaServiceInputValidationTests` (2 test), `VisitaServiceInputValidationTests` (2 test), `UtentePostgresDaoSecurityTests` (5 test), `ImmobilePostgresDaoSecurityTests` (7 test), `OffertaPostgresDaoSecurityTests` (8 test), `VisitaPostgresDaoSecurityTests` (7 test), `UtenteAgenziaPostgresDaoSecurityTests` (2 test) |
| **WSTG-INPV-11** | Type Confusion / Code Injection | ✓ Compliant | `OpenMeteoExceptionHandlingTests` (1 test) |
| **WSTG-INPV-13** | Email Header Injection / Large Payload | ✓ Compliant | `MalformedPayloadTests` (1 test), `MeteoServiceSecurityTests` (1 test), `EmailServiceSecurityTests` (2 test) |
| **WSTG-BUSL-07** | Business Logic Bypass | ✓ Compliant | `ImmobileServiceInputValidationTests` (6 test), `OffertaServiceInputValidationTests` (2 test), `VisitaServiceInputValidationTests` (2 test), `MeteoServiceSecurityTests` (6 test), `OffertaStateTransitionTests` (6 test), `VisitaStateTransitionTests` (6 test), `OffertaPostgresDaoSecurityTests` (1 test), `VisitaPostgresDaoSecurityTests` (1 test) |
| **WSTG-CRYP-04** | Sensitive Data at Persistence Layer | ✓ Compliant | `UtentePostgresDaoSecurityTests` (1 test) |
| **WSTG-SI-11** | Input Validation - XXE & SSRF | ✓ Compliant | `RegistrazioneRequestValidationTests` (10 test) |
| **WSTG-ERRH-01** | Error Handling - Information Disclosure | ✓ Compliant | `OffertaExceptionHandlingTests` (5 test), `VisitaExceptionHandlingTests` (5 test), `OffertaServiceInputValidationTests` (4 test), `ImmobileServiceInputValidationTests` (1 test), `VisitaServiceInputValidationTests` (3 test), `GeoDataExceptionHandlingTests` (4 test), `EmailServiceSecurityTests` (2 test), `OpenMeteoExceptionHandlingTests` (7 test), `ImmobilePostgresDaoSecurityTests` (1 test), `OffertaPostgresDaoSecurityTests` (1 test), `VisitaPostgresDaoSecurityTests` (1 test) |

---

## 7. Pipeline

Il progetto adotta una pipeline CI strutturata su due livelli: containerizzazione con Docker per garantire ambienti riproducibili, e automazione con GitHub Actions per l'esecuzione dei test, l'analisi della qualità del codice e l'aggiornamento automatico delle dipendenze.

### 7.1 Configurazione Progetto

Rispetto al main ho fatto in modo che il file application.properties leggesse da variabili d'ambiente, in modo da poter tener traccia anche di questo file su git, così da permettere la configurazione del progetto ugualmente per ogni build del progetto ed ottimizzato il build nelle pipeline.

C'è anche il file application-test.properties che viene usato per i test, con configurazione specifica per il database di test.

L'approccio che è stato utilizzato è `Fail-Fast Configuration`, ovvero in caso di variabili d'ambiente mancanti, l'applicazione fallisce al startup, così da evitare errori di configurazione che potrebbero portare a problemi di sicurezza o malfunzionamenti.

Le variabili d'ambiente sono salvate nel file .env iniettato nei container Docker, e sono documentate nel README con istruzioni per la generazione del JWT secret, la configurazione del db, delle email e del servizio Geoapify.

### 7.2 Docker

Il progetto è stato dockerizzato con due ambienti distinti, entrambi basati sull'immagine `maven:3.9-eclipse-temurin-21`:

- **Ambiente di produzione** (`Dockerfile` + `compose.yaml`): build multi-stage che produce un'immagine runtime minimale con solo il JAR dell'applicazione, affiancata da un container PostgreSQL.

- **Ambiente di test** (`Dockerfile.test` + `compose.test.yaml`): container dedicato che si avvia, esegue l'intera suite di test tramite `mvn clean test` e si ferma automaticamente. PostgreSQL viene avviato come servizio separato con healthcheck, garantendo che il database sia pronto prima dell'esecuzione dei test. I log e il risultato finale sono visibili direttamente nell'output di Docker Compose.

Questo approccio garantisce che i test vengano eseguiti nello stesso ambiente sia in locale che in CI, eliminando dipendenze dall'host e assicurando la riproducibilità dei risultati.

### 7.3 GitHub Actions

La configurazione di GitHub Actions è composta da tre file in `.github/`:

**`workflows/test.yml`** — si attiva ad ogni push sul branch `security` e ad ogni pull request. Esegue i seguenti step:
1. Build dell'immagine Docker di test con **Docker BuildKit**, sfruttando la cache dei layer su GitHub Actions: se `pom.xml` e `Dockerfile.test` non sono cambiati, il layer con le dipendenze Maven viene ripristinato dalla cache, evitando di riscaricarlo ad ogni run.
2. Esecuzione dei test tramite `docker compose up`. Al termine, il report di coverage generato da JaCoCo viene estratto dal container con `docker compose cp`, evitando conflitti con `mvn clean` che non può eliminare una directory montata come volume.
3. Upload del report `jacoco.xml` come artifact temporaneo (retention 1 giorno), reso disponibile al workflow successivo.

**`workflows/sonar.yml`** — si attiva automaticamente tramite `workflow_run` al completamento con successo di `test.yml`. Esegue:
1. Download del report JaCoCo prodotto dal workflow precedente.
2. Compilazione dei sorgenti con Maven (dipendenze cachate) per rendere disponibili le classi compilate all'analisi.
3. Analisi SonarQube che include la test coverage reale, precedentemente non disponibile perché i test richiedono il database PostgreSQL per essere eseguiti.

I due workflow appaiono come pipeline distinte nella dashboard di GitHub Actions, con storico e stato indipendenti.

**`dependabot.yml`** — configura Dependabot per il monitoraggio automatico delle dipendenze su tre ecosistemi:

- **Maven** — controlla `pom.xml` per aggiornamenti alle dipendenze Java/Spring Boot.
- **Docker** — controlla le base image nei `Dockerfile` per nuove versioni.
- **GitHub Actions** — controlla le versioni delle action usate nei workflow (es. `actions/checkout`, `actions/cache`).

Il primo di ogni mese Dependabot apre automaticamente PR separate per ogni aggiornamento disponibile. Le PR passano attraverso l'intera pipeline CI (`test.yml` → `sonar.yml`) prima del merge, garantendo che nessun aggiornamento rompa la build. Dependabot gestisce anche gli **aggiornamenti di sicurezza** in modo autonomo, aprendo PR urgenti in caso di vulnerabilità note indipendentemente dallo schedule mensile.

---

## 8. Esecuzione del progetto

### 8.1 Prerequisiti

- **Java 21+** (SDK)
- **Maven 3.8+**
- **Docker** e **Docker Compose**
- **OpenSSL** (per generare JWT secret)

### 8.2 Setup Iniziale

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

**Note su Geoapify**:
- Registrarsi su Geoapify per ottenere una API key

### 8.3 Avvio con Docker Compose

```bash
docker compose up -d
```

**Servizi in avvio**:
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432

### 8.4 Esecuzione dei Test con Docker Compose

```bash
docker compose -f compose.test.yaml up --build --abort-on-container-exit 2>&1 \
      | tr -d '\r' \
      | sed -E 's/\x1B\[[0-9;]*[[:alpha:]]//g' \
      | grep -E '^(\[)?backend-test(\])?[[:space:]]*\|.*(ERROR|FAILED|Caused by|Tests run:|BUILD SUCCESS|BUILD FAILURE|Started)'
```

---

## 9. Qualità del Codice

Placeholder

