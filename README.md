<div align="center">

# DietiEstates25Backend — Security Refactoring

[![CI](https://github.com/taekwondodev/DietiEstate25Backend/actions/workflows/ci.yml/badge.svg?branch=security)](https://github.com/taekwondodev/DietiEstate25Backend/actions/workflows/ci.yml)
[![Deploy](https://github.com/taekwondodev/DietiEstate25Backend/actions/workflows/deploy.yml/badge.svg)](https://github.com/taekwondodev/DietiEstate25Backend/actions/workflows/deploy.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=taekwondodev_DietiEstate25Backend&metric=alert_status&token=cff8cce96bb693f472e72257a51e903ed0e2416a)](https://sonarcloud.io/summary/new_code?id=taekwondodev_DietiEstate25Backend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=taekwondodev_DietiEstate25Backend&metric=coverage&token=cff8cce96bb693f472e72257a51e903ed0e2416a)](https://sonarcloud.io/summary/new_code?id=taekwondodev_DietiEstate25Backend)
![Dependabot](https://img.shields.io/badge/Dependabot-enabled-025E8C?style=flat-square&logo=dependabot&logoColor=white)

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-7.0.4-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9.13-C71A36?style=flat-square&logo=apachemaven&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-BOM-BC4521?style=flat-square&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Latest-2496ED?style=flat-square&logo=docker&logoColor=white)
![kind](https://img.shields.io/badge/kind-v0.29+-326CE5?style=flat-square&logo=kubernetes&logoColor=white)
![kubectl](https://img.shields.io/badge/kubectl-Latest-326CE5?style=flat-square&logo=kubernetes&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit_5-BOM-25A162?style=flat-square&logo=junit5&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo-0.8.14-E05D44?style=flat-square&logoColor=white)
![Geoapify](https://img.shields.io/badge/Geoapify-REST_API-FF6B35?style=flat-square&logoColor=white)
![Open Meteo](https://img.shields.io/badge/Open_Meteo-REST_API-00B4D8?style=flat-square&logoColor=white)

</div>

Branch dedicato al refactoring della sicurezza: AWS Cognito rimosso, autenticazione migrata in-house con Spring Security, JWT e PostgreSQL. Include la produzione del report di sicurezza.

> Per fare riferimento al README `precedente` all'avvio del progetto fare riferimento a [questo link](https://github.com/taekwondodev/DietiEstate25Backend/tree/main).

---

## Descrizione dell'Applicazione

DietiEstates25 è una piattaforma per la gestione e commercializzazione di proprietà immobiliari. Le funzionalità principali includono:

- **Autenticazione** — Registrazione e login di clienti e operatori
- **Gestione Immobili** — Creazione, ricerca e visualizzazione proprietà con filtri avanzati
- **Prenotazioni Visite** — Prenotazione di visite in immobili con notifica email
- **Sistema di Offerte** — Offerte economiche su immobili con tracciamento dello stato
- **Servizi Geospaziali** — Integrazione con **Geoapify** per coordinate GPS e punti di interesse
- **Previsioni Meteo** — Integrazione con **Open Meteo** per verificare il meteo e facilitare la scelta dei giorni migliori in cui programmare una visita.
- **Gestione Agenziale** — Sistema multi-tenant per agenzie immobiliari con ruoli differenziati

---

## Stack Tecnologico

| Tecnologia | Versione | Scopo                                                                                                                                       |
|---|---|---------------------------------------------------------------------------------------------------------------------------------------------|
| **Java** | 21 | Linguaggio principale                                                                                                                       |
| **Spring Boot** | 4.0.5 | Framework REST API                                                                                                                          |
| **Spring Security** | 7.0.4 | Autenticazione JWT e autorizzazione RBAC                                                                                                    |
| **Spring Data JDBC / JdbcTemplate** | (gestita da BOM) | Accesso database con SQL raw                                                                                                                |
| **Spring Validation** | (gestita da BOM) | Bean validation (Jakarta)                                                                                                                   |
| **Spring Mail** | (gestita da BOM) | Invio notifiche email                                                                                                                       |
| **Spring Actuator** | (gestita da BOM) | Health check ed esposizione metriche                                                                                                        |
| **PostgreSQL** | 16 | Database relazionale                                                                                                                        |
| **Lombok** | (gestita da BOM) | Riduzione boilerplate (getter, costruttori)                                                                                                 |
| **JUnit 5 + Mockito** | (gestita da BOM) | Testing unitario e di integrazione                                                                                                          |
| **JaCoCo** | 0.8.14 | Copertura del codice (report XML per SonarQube)                                                                                             |
| **Maven** | 3.9.13 | Build e gestione dipendenze                                                                                                                 |
| **Docker** | Latest | Containerizzazione per CI e ambienti di test                                                                                                |
| **kind** | v0.29+ | Cluster Kubernetes locale via Podman (macOS)                                                                                                |
| **kubectl** | Latest | CLI per interagire con il cluster kind                                                                                                      |
| **curl + jq** | Latest | Testing manuale delle API via `test-manual.sh`                                                                                              |
| **Geoapify** | REST API | Punti di interesse e dati geospaziali                                                                                                       |
| **Open Meteo** | REST API | Previsioni meteo per coordinate GPS                                                                                                         |
| **GitGuardian** | gg-shield | Secrets detection — API key, token, credenziali hardcoded nel codice e nella git history                                                    |
| **Snyk** | Cloud | SCA — CVE su dipendenze Maven con remediation advice + license compliance (GPL/AGPL detection)                                              |
| **Trivy** | v0.35.0 | Scansione CVE sui package OS dell'immagine Docker (HIGH/CRITICAL)                                                                           |
| **SonarCloud** | Cloud | Analisi statica del codice, quality gate e copertura                                                                                        |
| **Semgrep** | OSS | SAST pattern-based — ruleset `p/java` e `p/owasp-top-ten` per vulnerability pattern matching su sorgenti Java                               |
| **OWASP ZAP** | v0.15.0 / v0.10.0 | Analisi dinamica — baseline passivo e tre API scan autenticati per ruolo (Cliente, AgenteImmobiliare, Admin), alimentati dallo spec OpenAPI |
| **Dependabot** | GitHub | Aggiornamento automatico dipendenze Maven e GitHub Actions                                                                                  |

### Dipendenze ([pom.xml](backend/pom.xml))

#### Rimosse (vs. branch `main`)
- `software.amazon.awssdk:cognitoidentityprovider` — Cognito gestiva esternamente registrazione, login e validazione token. Rimossa perché l'intero stack di autenticazione è ora gestito internamente.

#### Aggiunte — Codice (vs. branch `main`)
- `spring-boot-starter-security` — **Prima**: Cognito si occupava di hashing delle password e del ciclo di vita dell'utente. **Ora**: BCrypt hashing e il `SecurityFilterChain` sono gestiti internamente da Spring Security.
- `spring-security-oauth2-jose:7.0.4` — **Prima**: Cognito emetteva e validava i token. **Ora**: i JWT sono firmati e verificati dal server tramite HMAC-SHA256 con un secret gestito localmente.
- `spring-boot-starter-oauth2-resource-server` — Necessario per configurare Spring Security come resource server JWT: intercetta ogni richiesta, estrae e valida il token prima che raggiunga il controller.
- `spring-boot-starter-restclient` — **Prima**: `RestTemplate` (deprecato). **Ora**: `RestClient`, l'API fluente e sincrona introdotta in Spring Boot 4 per le chiamate verso API esterne (Geoapify, Open Meteo).
- `spring-boot-starter-actuator` — Aggiunto per esporre `/actuator/health` e metriche applicative, necessari per health check in Kubernetes e osservabilità nella pipeline CI/CD.

#### Aggiunte — Test (vs. branch `main`)
- `spring-security-test` — **Prima**: nessun test di sicurezza (Cognito era esterno). **Ora**: MockMvc può simulare richieste autenticate con `SecurityContext` per testare la filter chain.
- `spring-boot-starter-webmvc-test` — Artifact separato introdotto in Spring Boot 4 per `@WebMvcTest`; era incluso implicitamente nelle versioni precedenti.

---

## Architettura e Ruoli

### Struttura Organizzativa

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

| Ruolo | Descrizione                                                |
|-------|------------------------------------------------------------|
| **Unauthenticated** | Utente non autenticato           |
| **Admin** | Amministratore di agenzia |
| **Gestore** | Responsabile operativo agenzia    |
| **AgenteImmobiliare** | Agente di commercializzazione  |
| **Cliente** | Utente finale   |

### Matrice di Accesso (Endpoint)

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
| `GET /offerta/riepilogoCliente` | ✗ | ✗ | ✗ | ✗ | ✓ |
| `GET /offerta/riepilogoUtenteAgenzia` | ✗ | ✓ | ✓ | ✓ | ✗ |
| `POST /visita/prenota` | ✗ | ✓ | ✓ | ✓ | ✓ |
| `PATCH /visita/aggiorna` | ✗ | ✓ | ✓ | ✓ | ✓ |
| `GET /visita/riepilogoCliente` | ✗ | ✗ | ✗ | ✗ | ✓ |
| `GET /visita/riepilogoUtenteAgenzia` | ✗ | ✓ | ✓ | ✓ | ✗ |

---

## Autenticazione e Sicurezza

Autenticazione migrata da AWS Cognito a Spring Security in-house: JWT emessi e validati internamente, password hashate con BCrypt, filter chain configurata stateless.

→ [docs/autenticazione/README.md](docs/autenticazione/README.md)

---

## Testing

348 test di sicurezza organizzati in cinque aree: input validation, authorization & access control, data protection, business logic e DAO integration. Strategia basata su OWASP Testing Guide.

→ [docs/testing/README.md](docs/testing/README.md)

## Pipeline

Pipeline CI strutturata su due livelli: Docker per ambienti riproducibili e GitHub Actions per l'orchestrazione di sette domini di sicurezza distinti — secrets detection (GitGuardian), analisi statica dataflow (SonarCloud), analisi statica pattern-based (Semgrep), SCA con license compliance (Snyk), DAST (OWASP ZAP), container security (Trivy image scan) — più deploy automatico su Docker Hub al completamento di tutti i check. Dependabot monitora dipendenze Maven, Docker e Actions.

→ [docs/pipeline/README.md](docs/pipeline/README.md)

## Secrets

Variabili d'ambiente sensibili (DB, JWT, SMTP, Geoapify) iniettate a runtime tramite Kubernetes Secrets non versionati. Variabile mancante → crash allo startup (Fail-Fast). 

Lista completa, generazione e setup locale → [docs/secrets/README.md](docs/secrets/README.md).

---

## Kubernetes

Setup locale con kind + Podman. PostgreSQL isolato via NetworkPolicy e ClusterIP, credenziali iniettate tramite Kubernetes Secrets non versionati, namespace di test separato dalla produzione.

- Comandi operativi → [k8s/README.md](k8s/README.md)
- Decisioni di design e di sicurezza → [docs/k8s/README.md](docs/k8s/README.md)


