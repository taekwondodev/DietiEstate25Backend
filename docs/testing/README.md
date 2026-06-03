# Testing

Il testing è **incentrato sulla sicurezza**, non sulla correttezza funzionale. Strategia basata su **OWASP Testing Guide**.

La strategia segue e copre quattro aree:

| Area | Test |
|------|------|
| Input Validation & Boundary Testing | 38 |
| Authorization & Access Control | 90 |
| Data Protection | 42 |
| Business Logic Security | 130 |
| DAO Integration Security | 48 |
| **TOTALE** | **348** |

---

## Input Validation & Boundary Testing

**[MalformedPayloadTests](../../backend/src/test/java/com/dietiestate25backend/security/validation/MalformedPayloadTests.java)**

> `POST /auth/login` · `POST /auth/register`

**Outcome:** `GlobalExceptionHandler` esteso con handler per `HttpMessageNotReadableException` e `MethodArgumentTypeMismatchException`. Limiti `@Size` / `@DecimalMax` aggiunti ai DTO. `@Pattern(regexp = "^[\\x20-\\x7E]+$")` su `password` rifiuta Unicode non-ASCII.

---

**[LoginRequestValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/validation/LoginRequestValidationTests.java)**

> `POST /auth/login`

**Outcome:** `@NotNull`, `@NotBlank`, `@Email` su `LoginRequest`. Jackson `FAIL_ON_UNKNOWN_PROPERTIES=true` rifiuta campi extra.

---

**[RegistrazioneRequestValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/validation/RegistrazioneRequestValidationTests.java)**

> `POST /auth/register`

**Outcome:** stessa struttura di `LoginRequest` con aggiunta di `@NotNull` su `role`.

### Miglioramenti Implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **Exception Handling** | Errori 500 generici | Risposte 400/401/404 tipizzate | Information hiding |
| **Input Validation - DTO** | Solo `@NotNull` | `@NotBlank`, `@Size`, `@Pattern`, `@Email`, `@Min`/`@Max`, `@Positive` | Boundary protection |
| **Input Validation - Query Params** | Nessuna | `@Min`, `@Positive`, `@NotBlank` su `@RequestParam` | Parameter tampering prevention |
| **Error Messages** | Specifici dell'implementazione | Business-friendly, nessun dettaglio tecnico | Security by obscurity |
| **DAO Error Mapping** | `RuntimeException` non gestita | `DataIntegrityViolationException` → `ConflictException` | Constraint violation handling |
| **Type Coercion Safety** | Nessuna validazione | `MethodArgumentTypeMismatchException` handler | Type safety |

---

## Authorization & Access Control

I test di questa sezione verificano **ogni cella** della [Matrice di Accesso (Endpoint)](../../README.md#matrice-di-accesso-endpoint): per ciascuno dei 16 endpoint e per ciascuno dei 5 attori (Unauthenticated, Cliente, AgenteImmobiliare, Gestore, Admin) viene asserito il comportamento atteso (✓ / ✗ con codice HTTP esatto).

**[PublicEndpointTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/PublicEndpointTests.java)**

| Endpoint | Senza JWT | Con JWT |
|----------|:---------:|:-------:|
| `POST /auth/login` | ✓ | ✓ |
| `POST /auth/register` | ✓ | ✓ |
| `GET /immobile/cerca` | ✓ | ✓ |
| `POST /auth/register-staff` | ✗ 401 | ✓ |
| `GET /immobile/personali` | ✗ 401 | ✓ |
| `POST /immobile/crea` | ✗ 401 | ✓ |
| `POST /visita/prenota` | ✗ 401 | ✓ |

**Outcome:** `SecurityFilterChain` con `permitAll()` esplicito per i tre endpoint pubblici; fallback `anyRequest().authenticated()`.

---

**[AdminBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/AdminBoundaryTests.java)**

> `POST /auth/register-staff`

| Unauthenticated | Cliente | Agente | Gestore | Admin |
|:-:|:-:|:-:|:-:|:-:|
| ✗ 401 | ✗ 401 | ✗ 401 | ✓ 200 | ✓ 200 |

**Outcome:** [`TokenUtils.checkIfAdminOrGestore()`](../../backend/src/main/java/com/dietiestate25backend/utils/TokenUtils.java) in `AuthController`.

---

**[UtenteAgenziaBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/UtenteAgenziaBoundaryTests.java)**

> `POST /immobile/crea` · `GET /immobile/personali`

| Unauthenticated | Cliente | Admin | Gestore | Agente |
|:-:|:-:|:-:|:-:|:-:|
| ✗ 401 | ✗ 403 | ✓ | ✓ | ✓ |

**Outcome:** [`TokenUtils.checkIfUtenteAgenzia()`](../../backend/src/main/java/com/dietiestate25backend/utils/TokenUtils.java) in `ImmobileController`.

---

**[GeodataBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/GeodataBoundaryTests.java)** · **[MeteoBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/MeteoBoundaryTests.java)**

> `POST /geodata` · `POST /meteo`

| Unauthenticated | Cliente | Admin | Gestore | Agente |
|:-:|:-:|:-:|:-:|:-:|
| ✗ 401 | ✓ | ✓ | ✓ | ✓ |

**Outcome:** `anyRequest().authenticated()` — nessuna restrizione di ruolo aggiuntiva.

---

**[OffertaBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/OffertaBoundaryTests.java)** · **[VisitaBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/VisitaBoundaryTests.java)**

> `POST /offerta/aggiungi` · `PATCH /offerta/aggiorna` · `POST /visita/prenota` · `PATCH /visita/aggiorna`

| Unauthenticated | Cliente | Admin | Gestore | Agente |
|:-:|:-:|:-:|:-:|:-:|
| ✗ 401 | ✓ | ✓ | ✓ | ✓ |

**Outcome:** `anyRequest().authenticated()` — nessuna restrizione di ruolo aggiuntiva.

---

**[ImmobileOwnershipTests](../../backend/src/test/java/com/dietiestate25backend/security/dataisolation/ImmobileOwnershipTests.java)** · **[OffertaPrivacyTests](../../backend/src/test/java/com/dietiestate25backend/security/dataisolation/OffertaPrivacyTests.java)** · **[VisitaPrivacyTests](../../backend/src/test/java/com/dietiestate25backend/security/dataisolation/VisitaPrivacyTests.java)**

| Endpoint | Unauthenticated | Cliente | Admin/Gestore/Agente |
|----------|:---------------:|:-------:|:--------------------:|
| `GET /offerta/riepilogoCliente` | ✗ 401 | ✓ | ✗ 403 |
| `GET /offerta/riepilogoUtenteAgenzia` | ✗ 401 | ✗ 403 | ✓ |
| `GET /visita/riepilogoCliente` | ✗ 401 | ✓ | ✗ 403 |
| `GET /visita/riepilogoUtenteAgenzia` | ✗ 401 | ✗ 403 | ✓ |
| `GET /immobile/personali` | ✗ 401 | ✗ 403 | ✓ (solo propri) |

> **Bug fixato:** `/riepilogoUtenteAgenzia` era accessibile ai Cliente. Aggiunta guardia [`TokenUtils.checkIfUtenteAgenzia()`](../../backend/src/main/java/com/dietiestate25backend/utils/TokenUtils.java).

---

**[BruteForceAndAccountLockoutTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/BruteForceAndAccountLockoutTests.java)**

> `POST /auth/login`

| Scenario | Comportamento |
|----------|--------------|
| 5 tentativi falliti consecutivi | Account bloccato per 15 minuti |
| Login con account bloccato | Rifiutato anche con password corretta |
| Scadenza lockout | Sblocco automatico |
| Login riuscito dopo fallimenti | Counter azzerato |
| Messaggio di errore su account bloccato | Generico — non rivela stato account |
| Trigger lockout | Email di notifica inviata |

**Outcome:** [`Utente.failedLoginAttempts`](../../backend/src/main/java/com/dietiestate25backend/model/Utente.java#L18) · [`UtentePostgres.updateLoginAttempts()`](../../backend/src/main/java/com/dietiestate25backend/dao/postgresimplements/UtentePostgres.java#L27) · [`AuthService.login()`](../../backend/src/main/java/com/dietiestate25backend/service/AuthService.java#L39)

### Miglioramenti Implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **RBAC** | Nessun controllo di ruolo | `checkIfAdminOrGestore()`, `checkIfUtenteAgenzia()`, `checkIfCliente()` | Authorization enforcement |
| **Data Isolation** | Cliente accedeva a `riepilogoUtenteAgenzia` | Bloccato con `checkIfUtenteAgenzia()` | Privacy protection |
| **Brute Force Protection** | Nessuna | 5 tentativi max + 15 min lockout | Attack prevention |
| **Account Lockout** | Nessun lockout | Auto-lock + email notification | Credential protection |

---

## Data Protection

**[JwtServiceSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/auth/JwtServiceSecurityTests.java)**

> tutti gli endpoint protetti

| Scenario | Comportamento |
|----------|--------------|
| Token con claims corretti (`sub`, `role`, `email`) | Generato e decodificato correttamente |
| Token scaduto | `UnauthorizedException` — claims non estraibili |
| Firma modificata / secret diverso | Token rifiutato |
| JWT con `role=Admin` forgiato | HTTP 401 — firma invalida |

**Outcome:** [`JwtService.generateToken()`](../../backend/src/main/java/com/dietiestate25backend/service/JwtService.java#L30) con `JwtEncoder` / `JwtDecoder`; `JwtException` → `UnauthorizedException`.

---

**[AuthServiceSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/auth/AuthServiceSecurityTests.java)**

> `POST /auth/login` · `POST /auth/register`

| Scenario | Comportamento |
|----------|--------------|
| Password hashata prima di persistere | Hash BCrypt nel DB, mai plaintext |
| Stessa password → hash diversi | Salt random per ogni registrazione |
| Password errata o email inesistente | Stesso messaggio generico — nessuna user enumeration |
| Tempo di verifica con password errata | Costante — BCrypt anti-timing attack |

---

**[PasswordPolicySecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/auth/PasswordPolicySecurityTests.java)**

> `POST /auth/register`

| Regola | Soglia |
|--------|--------|
| Lunghezza | 8–255 caratteri |
| Complexity | Maiuscola + minuscola + numero + carattere speciale (`@$!%*?&`) |
| Pattern | No sequenziali, no parti dell'email |
| Whitespace | Spazi interni e leading/trailing rifiutati |

**Outcome:** [`@Pattern` in `RegistrazioneRequest.password`](../../backend/src/main/java/com/dietiestate25backend/dto/requests/RegistrazioneRequest.java#L18)

### Miglioramenti Implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **Password Storage** | Rischio plaintext | BCrypt con salt random | Credential protection |
| **Error Messages** | Specifici per email/password | Generici | User enumeration prevention |
| **Token Expiration** | Non implementato | 1 ora con `expiresAt` | Session timeout |
| **Password Strength** | Nessuna | 8+ char, uppercase, lowercase, digit, special | OWASP WSTG-AUTHN-07 |

---

## Business Logic Security

**[OffertaStateTransitionTests](../../backend/src/test/java/com/dietiestate25backend/security/business/OffertaStateTransitionTests.java)** · **[VisitaStateTransitionTests](../../backend/src/test/java/com/dietiestate25backend/security/business/VisitaStateTransitionTests.java)**

> `PATCH /offerta/aggiorna` · `PATCH /visita/aggiorna`

| Da \ A | IN_SOSPESO | ACCETTATA/CONFERMATA | RIFIUTATA |
|--------|:---------:|:--------------------:|:---------:|
| **IN_SOSPESO** | ✗ | ✓ | ✓ |
| **ACCETTATA/CONFERMATA** | ✗ | ✗ | ✗ |
| **RIFIUTATA** | ✗ | ✗ | ✗ |

**Outcome:** `OffertaService.isTransazioneValida()` · `VisitaService.isTransizioneValidaVisita()`

---

**[OffertaExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/OffertaExceptionHandlingTests.java)** · **[VisitaExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/VisitaExceptionHandlingTests.java)** · **[ImmobileExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/ImmobileExceptionHandlingTests.java)** · **[AuthServiceExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/AuthServiceExceptionHandlingTests.java)** · **[JwtServiceExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/JwtServiceExceptionHandlingTests.java)** · **[GeoapifyGeoDataExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/GeoapifyGeoDataExceptionHandlingTests.java)** · **[OpenMeteoExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/OpenMeteoExceptionHandlingTests.java)** · **[EmailServiceSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/business/EmailServiceSecurityTests.java)** · **[GeoDataExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/GeoDataExceptionHandlingTests.java)**

**Outcome comune:** ownership check eseguito **prima** della validazione stato; errori DAO wrappati in eccezioni business senza propagare stack trace; messaggi identici per scenari diversi (es. email inesistente = password errata = stesso messaggio).

---

**[ImmobileServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/ImmobileServiceInputValidationTests.java)** · **[OffertaServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/OffertaServiceInputValidationTests.java)** · **[VisitaServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/VisitaServiceInputValidationTests.java)** · **[MeteoServiceSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/business/MeteoServiceSecurityTests.java)**

**Outcome:** controlli bloccano payload fuori range, UID tamperati, stati iniettati e transizioni illegali **prima di qualsiasi interazione con il database**.

> **Bug risolto:** condizione date range invertita in `MeteoService` — rifiutava le date valide e accettava quelle oltre i 7 giorni.

### Miglioramenti Implementati

| Area | Prima | Dopo | Impact |
|------|-------|------|--------|
| **State Transition** | Nessuna validazione | State machine in service layer | Business logic integrity |
| **Exception Leakage** | `"Errore interno: " + e.getMessage()` | `"Errore interno del server"` | Information disclosure prevention |
| **Guard Ordering** | Stato verificato prima di ownership | Ownership verificato **prima** di stato | Information hiding |
| **API External Errors** | Stack trace leakato | `InternalServerErrorException` generico | External service details protection |
| **Date Range Validation** | Condizione invertita | `isBefore(oggi) \|\| isAfter(oggi+7)` | Correct temporal boundary |

---

## DAO Integration Security

Tutti i test usano `@Transactional` con rollback automatico — isolamento garantito rispetto ai dati di `02_test_data.sql`.

**[UtentePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtentePostgresDaoSecurityTests.java)** · **[ImmobilePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/ImmobilePostgresDaoSecurityTests.java)** · **[OffertaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/OffertaPostgresDaoSecurityTests.java)** · **[VisitaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/VisitaPostgresDaoSecurityTests.java)** · **[UtenteAgenziaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtenteAgenziaPostgresDaoSecurityTests.java)**

**Outcome:** query parametrizzate JdbcTemplate neutralizzano OR, UNION, comment e stacked query injection su tutti i metodi testati. FK constraint verifica DB-level la validità dei riferimenti. Transizioni di stato persistite e verificate con ri-lettura.

### Bug Risolti durante il Testing DAO

| Bug | File | Problema | Fix |
|-----|------|----------|-----|
| Colonne DB errate in UPDATE | [`UtentePostgres`](../../backend/src/main/java/com/dietiestate25backend/dao/postgresimplements/UtentePostgres.java) | `failed_login_attempts` / `locked_until` inesistenti | Corrette in `failedloginattempts` / `lockeduntil` |
| SELECT incompleto in `findByEmail` | [`UtentePostgres`](../../backend/src/main/java/com/dietiestate25backend/dao/postgresimplements/UtentePostgres.java) | RowMapper leggeva colonne non presenti nel SELECT | Aggiunte alla clausola SELECT |
| Enum mismatch con PostgreSQL | [`StatoOfferta`](../../backend/src/main/java/com/dietiestate25backend/model/StatoOfferta.java) / [`StatoVisita`](../../backend/src/main/java/com/dietiestate25backend/model/StatoVisita.java) | `"In sospeso"` vs `'In Sospeso'` nel tipo ENUM | Corretta capitalizzazione |
| JDBC VARCHAR → ENUM cast mancante | [`OffertaPostgres`](../../backend/src/main/java/com/dietiestate25backend/dao/postgresimplements/OffertaPostgres.java) / [`VisitaPostgres`](../../backend/src/main/java/com/dietiestate25backend/dao/postgresimplements/VisitaPostgres.java) | PostgreSQL rifiutava `character varying` per colonne ENUM | Aggiunto `CAST(? AS statoofferta)` / `CAST(? AS statovisita)` |

---

## Conformità OWASP Testing Guide

| Sezione OWASP WSTG | Descrizione | Status | Suite |
|---|---|---|---|
| **WSTG-AUTHN-04** | Testing for Bypassing Authentication Schema — JWT Token Integrity | ✓ | [JwtServiceSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/auth/JwtServiceSecurityTests.java) |
| **WSTG-AUTHN-03** | Testing for Weak Lock Out Mechanism — Brute Force Protection | ✓ | [BruteForceAndAccountLockoutTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/BruteForceAndAccountLockoutTests.java) |
| **WSTG-AUTHN-07** | Testing for Weak Password Policy | ✓ | [PasswordPolicySecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/auth/PasswordPolicySecurityTests.java) |
| **WSTG-AUTHZ-03** | Privilege Escalation — Role-Based Access Control | ✓ | [AdminBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/AdminBoundaryTests.java) · [UtenteAgenziaBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/UtenteAgenziaBoundaryTests.java) · [GeodataBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/GeodataBoundaryTests.java) · [MeteoBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/MeteoBoundaryTests.java) · [OffertaBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/OffertaBoundaryTests.java) · [VisitaBoundaryTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/VisitaBoundaryTests.java) |
| **WSTG-AUTHZ-02** | Bypassing Authorization Schema — Data Isolation | ✓ | [ImmobileOwnershipTests](../../backend/src/test/java/com/dietiestate25backend/security/dataisolation/ImmobileOwnershipTests.java) · [OffertaPrivacyTests](../../backend/src/test/java/com/dietiestate25backend/security/dataisolation/OffertaPrivacyTests.java) · [VisitaPrivacyTests](../../backend/src/test/java/com/dietiestate25backend/security/dataisolation/VisitaPrivacyTests.java) · [UtentePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtentePostgresDaoSecurityTests.java) · [ImmobilePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/ImmobilePostgresDaoSecurityTests.java) · [OffertaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/OffertaPostgresDaoSecurityTests.java) · [VisitaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/VisitaPostgresDaoSecurityTests.java) · [UtenteAgenziaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtenteAgenziaPostgresDaoSecurityTests.java) |
| **WSTG-CONF-05** | Enumerate Infrastructure and Application Admin Interfaces | ✓ | [PublicEndpointTests](../../backend/src/test/java/com/dietiestate25backend/security/authorization/PublicEndpointTests.java) |
| **WSTG-INPV-01** | Testing for Reflected Cross-Site Scripting (XSS) | ✓ | [RegistrazioneRequestValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/validation/RegistrazioneRequestValidationTests.java) · [ImmobileServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/ImmobileServiceInputValidationTests.java) · [OffertaServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/OffertaServiceInputValidationTests.java) · [VisitaServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/VisitaServiceInputValidationTests.java) · [MeteoServiceSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/business/MeteoServiceSecurityTests.java) · [UtentePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtentePostgresDaoSecurityTests.java) · [ImmobilePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/ImmobilePostgresDaoSecurityTests.java) · [OffertaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/OffertaPostgresDaoSecurityTests.java) · [VisitaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/VisitaPostgresDaoSecurityTests.java) · [UtenteAgenziaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtenteAgenziaPostgresDaoSecurityTests.java) |
| **WSTG-INPV-05** | SQL Injection | ✓ | [RegistrazioneRequestValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/validation/RegistrazioneRequestValidationTests.java) · [MalformedPayloadTests](../../backend/src/test/java/com/dietiestate25backend/security/validation/MalformedPayloadTests.java) · [LoginRequestValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/validation/LoginRequestValidationTests.java) · [ImmobileServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/ImmobileServiceInputValidationTests.java) · [OffertaServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/OffertaServiceInputValidationTests.java) · [VisitaServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/VisitaServiceInputValidationTests.java) · [UtentePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtentePostgresDaoSecurityTests.java) · [ImmobilePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/ImmobilePostgresDaoSecurityTests.java) · [OffertaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/OffertaPostgresDaoSecurityTests.java) · [VisitaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/VisitaPostgresDaoSecurityTests.java) · [UtenteAgenziaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtenteAgenziaPostgresDaoSecurityTests.java) |
| **WSTG-INPV-10** | Testing for IMAP/SMTP Injection — Email Header Security | ✓ | [EmailServiceSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/business/EmailServiceSecurityTests.java) |
| **WSTG-BUSL-07** | Business Logic Bypass | ✓ | [OffertaStateTransitionTests](../../backend/src/test/java/com/dietiestate25backend/security/business/OffertaStateTransitionTests.java) · [VisitaStateTransitionTests](../../backend/src/test/java/com/dietiestate25backend/security/business/VisitaStateTransitionTests.java) · [ImmobileServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/ImmobileServiceInputValidationTests.java) · [OffertaServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/OffertaServiceInputValidationTests.java) · [VisitaServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/VisitaServiceInputValidationTests.java) · [UtentePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtentePostgresDaoSecurityTests.java) · [ImmobilePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/ImmobilePostgresDaoSecurityTests.java) · [OffertaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/OffertaPostgresDaoSecurityTests.java) · [VisitaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/VisitaPostgresDaoSecurityTests.java) · [UtenteAgenziaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtenteAgenziaPostgresDaoSecurityTests.java) |
| **WSTG-CRYP-04** | Secure Credential Storage — BCrypt Hashing & Anti-Enumeration | ✓ | [AuthServiceSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/auth/AuthServiceSecurityTests.java) · [UtentePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtentePostgresDaoSecurityTests.java) |
| **WSTG-ERRH-01** | Error Handling — Information Disclosure | ✓ | [OffertaExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/OffertaExceptionHandlingTests.java) · [VisitaExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/VisitaExceptionHandlingTests.java) · [ImmobileExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/ImmobileExceptionHandlingTests.java) · [AuthServiceExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/AuthServiceExceptionHandlingTests.java) · [JwtServiceExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/JwtServiceExceptionHandlingTests.java) · [GeoapifyGeoDataExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/GeoapifyGeoDataExceptionHandlingTests.java) · [OpenMeteoExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/OpenMeteoExceptionHandlingTests.java) · [EmailServiceSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/business/EmailServiceSecurityTests.java) · [GeoDataExceptionHandlingTests](../../backend/src/test/java/com/dietiestate25backend/security/business/GeoDataExceptionHandlingTests.java) · [ImmobileServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/ImmobileServiceInputValidationTests.java) · [OffertaServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/OffertaServiceInputValidationTests.java) · [VisitaServiceInputValidationTests](../../backend/src/test/java/com/dietiestate25backend/security/business/VisitaServiceInputValidationTests.java) · [UtentePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtentePostgresDaoSecurityTests.java) · [ImmobilePostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/ImmobilePostgresDaoSecurityTests.java) · [OffertaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/OffertaPostgresDaoSecurityTests.java) · [VisitaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/VisitaPostgresDaoSecurityTests.java) · [UtenteAgenziaPostgresDaoSecurityTests](../../backend/src/test/java/com/dietiestate25backend/security/dao/UtenteAgenziaPostgresDaoSecurityTests.java) |
