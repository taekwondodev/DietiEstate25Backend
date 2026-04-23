## 7. Pipeline

Il progetto adotta una pipeline CI strutturata su tre livelli: containerizzazione con Docker per garantire ambienti riproducibili, orchestrazione con Kubernetes per il deploy locale e l'esecuzione dei test, e automazione con GitHub Actions per l'esecuzione dei test, l'analisi della qualità del codice e l'aggiornamento automatico delle dipendenze.

### 7.2 Docker

Il progetto utilizza due Dockerfile distinti, entrambi basati sull'immagine `maven:3.9-eclipse-temurin-21`:

- **`Dockerfile`**: build multi-stage che produce un'immagine runtime minimale con solo il JAR dell'applicazione. Pubblicata automaticamente su **Docker Hub** (`taekwondodev/dietiestate25-backend`) dalla pipeline di deploy al termine di tutti i check di qualità e sicurezza.

- **`Dockerfile.test`**: immagine dedicata che esegue l'intera suite di test tramite `mvn clean test` e termina. Utilizzata dal Job Kubernetes nell'ambiente di test — non viene pubblicata su alcun registry esterno, viene caricata direttamente nel nodo kind tramite `kind load image-archive`.

### 7.3 Kubernetes

Il deploy locale avviene su un cluster kind (Kubernetes in Docker) gestito con Podman. L'infrastruttura è definita nei manifest in `k8s/` e prevede due namespace isolati:

- **`dietiestate25`** — ambiente di produzione: backend + PostgreSQL con NetworkPolicy che limita l'accesso al database al solo pod backend.
- **`dietiestate25-test`** — ambiente di test: PostgreSQL dedicato + Job che esegue la suite di test e termina, anch'esso protetto da NetworkPolicy.

Per il setup completo, i comandi di deploy e le istruzioni operative vedere [`k8s/README.md`](k8s/README.md).

### 7.4 GitHub Actions

La configurazione di GitHub Actions è composta da sei file in `.github/`. Un orchestratore centrale (`ci.yml`) chiama quattro workflow in sequenza; il deploy è separato e si attiva al completamento di CI:

```
push / pull_request
        │
        ▼
       CI ──────────────────────────────────── (workflow_run on CI success)
        ├── 1. test   → build + test + JaCoCo          │
        ├── 2. sast   → analisi SonarQube               │
        ├── 3. dast   → OWASP ZAP (baseline + API x3)  │
        └── 4. trivy  → filesystem scan + image scan    ▼
                                                     Deploy → Docker Hub
```

Il deploy viene eseguito solo se **tutti** gli step di CI completano con successo: test, analisi statica, DAST e scansione Trivy.

**`workflows/ci.yml`** — orchestratore della pipeline di verifica. Si attiva ad ogni push sul branch `security` e ad ogni pull request. Chiama in sequenza (via `uses:` + `needs:`) quattro workflow riutilizzabili: `test.yml` → `sonar.yml` → `zap.yml` → `trivy.yml`. Appare come voce autonoma nella dashboard di GitHub Actions.

**`workflows/test.yml`** — reusable workflow (`workflow_call`), chiamato da `ci.yml`. Esegue i seguenti step:
1. Build dell'immagine Docker di test con **Docker BuildKit**, sfruttando la cache dei layer su GitHub Actions: se `pom.xml` e `Dockerfile.test` non sono cambiati, il layer con le dipendenze Maven viene ripristinato dalla cache, evitando di riscaricarlo ad ogni run.
2. Esecuzione dei test tramite `docker compose up`. Al termine, il report di coverage generato da JaCoCo viene estratto dal container con `docker compose cp`, evitando conflitti con `mvn clean` che non può eliminare una directory montata come volume.
3. Upload del report `jacoco.xml` come artifact temporaneo (retention 1 giorno), reso disponibile al workflow successivo.

**`workflows/sonar.yml`** — reusable workflow (`workflow_call`), chiamato da `ci.yml` dopo `test`. Contiene un singolo job:

1. **`sonar`**: scarica il report JaCoCo (prodotto dal job `test` nella stessa workflow run), compila i sorgenti con Maven e lancia l'analisi SonarQube con coverage reale (non disponibile senza database PostgreSQL).

**`workflows/deploy.yml`** — si attiva tramite `workflow_run` al completamento con successo di `CI`. Appare come voce separata nella dashboard. Contiene un singolo job:

1. **`deploy`**: builda l'immagine di produzione con Docker BuildKit e la pubblica su Docker Hub con due tag: `latest` e il SHA del commit (`taekwondodev/dietiestate25-backend:<sha>`), garantendo tracciabilità e possibilità di rollback. Richiede i secret `DOCKERHUB_USERNAME` e `DOCKERHUB_TOKEN` configurati nel repository.

**`workflows/zap.yml`** — reusable workflow (`workflow_call`), richiamato da `ci.yml`. Esegue un singolo job (`zap`) che condivide setup e teardown tra tutte le scansioni:

1. Build dell'immagine di produzione con Docker BuildKit (cache condivisa con la chiave `buildx-dast-*`).
2. Avvio di PostgreSQL e MailHog (SMTP mock) tramite `compose.dast.yaml`, con tutte le variabili d'ambiente necessarie iniettate direttamente nel container. Il DB viene inizializzato con le fixture di test in `db-init/02_test_data.sql`, che includono utenti precostituiti per ogni ruolo.
3. **Baseline Scan**: scansione **passiva** con [OWASP ZAP](https://www.zaproxy.org/) — intercetta e analizza il traffico HTTP senza inviare payload aggressivi. Individua vulnerabilità di configurazione, header di sicurezza mancanti e informazioni esposte. Tipicamente completa in 2–5 minuti.
4. **API Scan (Cliente / AgenteImmobiliare / Admin)**: tre scansioni **attive** sequenziali, ciascuna autenticata con un ruolo diverso. ZAP legge `backend/openapi.yaml` per scoprire tutti gli endpoint definiti nell'API — anziché affidarsi allo spider — garantendo coverage completa anche sugli endpoint protetti da JWT che risponderebbero altrimenti con `401`. Prima di ogni scan, uno step dedicato effettua il login tramite `POST /auth/login` con le credenziali del ruolo corrispondente, maschera il token JWT nei log con `::add-mask::` e lo inietta in tutte le richieste ZAP tramite il Replacer add-on (`Authorization: Bearer <token>`). In questo modo ZAP raggiunge e testa gli endpoint protetti da RBAC che sarebbero altrimenti irraggiungibili. I finding comuni a più ruoli emergono in più report, aumentando la priorità percepita. Ogni API scan invia payload di attacco reali (SQLi, XSS, path traversal, CSRF, header injection, ecc.) verso ogni endpoint per verificare se l'applicazione risponde in modo vulnerabile.
5. Tear down dell'ambiente con `-v`, eseguito sempre indipendentemente dall'esito.

I finding noti e intenzionali vengono soppressi tramite `.zap/rules.tsv` in tutte le scansioni: `10049` (Non-Storable Content — `Cache-Control: no-store` è il comportamento corretto per un backend API) e `40042` (Spring Actuator Health — endpoint liveness/readiness intenzionale per Kubernetes).

Ogni scansione pubblica i risultati in una **GitHub Issue** dedicata (creata o aggiornata ad ogni run) e archivia i report HTML/JSON come artifact separato del workflow (`zap-baseline-report`, `zap-api-scan-cliente`, `zap-api-scan-agente`, `zap-api-scan-admin`).

Nella dashboard di GitHub Actions appaiono **due pipeline distinte**: **CI** e **Deploy**. I workflow `test.yml`, `sonar.yml`, `zap.yml` e `trivy.yml` sono privi di trigger autonomi e vengono eseguiti come reusable workflow orchestrati da `ci.yml`. Il `deploy.yml` è invece autonomo, con trigger `workflow_run` su `CI`.

**`dependabot.yml`** — configura Dependabot per il monitoraggio automatico delle dipendenze su tre ecosistemi:

- **Maven** — controlla `pom.xml` per aggiornamenti alle dipendenze Java/Spring Boot.
- **Docker** — controlla le base image nei `Dockerfile` e `Dockerfile.test` per nuove versioni.
- **GitHub Actions** — controlla le versioni delle action usate nei workflow (es. `actions/checkout`, `actions/cache`).

Il primo di ogni mese Dependabot apre automaticamente PR separate per ogni aggiornamento disponibile. Le PR passano attraverso l'intera pipeline CI (`ci.yml`) prima del merge, garantendo che nessun aggiornamento rompa la build. Dependabot gestisce anche gli **aggiornamenti di sicurezza** in modo autonomo, aprendo PR urgenti in caso di vulnerabilità note indipendentemente dallo schedule mensile.

**`workflows/trivy.yml`** — si attiva ad ogni push sul branch `security` e ad ogni pull request. Esegue due job in parallelo:

- **Filesystem Scan** (`trivy-fs`): scansiona l'intero repository alla ricerca di secrets hardcodati nei file sorgente, misconfiguration nei Dockerfile e Docker Compose, e CVE nelle dipendenze Maven dichiarate in `pom.xml`. I risultati vengono caricati nel tab *Security → Code scanning alerts* di GitHub in formato SARIF.

- **Image Scan** (`trivy-image`): builda l'immagine di produzione (`Dockerfile`) e scansiona i package OS del layer runtime (`eclipse-temurin:25-jre-jammy`) e le librerie Java embedded nel fat JAR. Sfrutta la stessa strategia di cache Docker BuildKit usata in `test.yml`, con una chiave separata per evitare collisioni. I risultati vengono caricati anch'essi come SARIF.

Entrambi i job falliscono con `exit-code: 1` in presenza di CVE HIGH o CRITICAL con fix disponibile, bloccando il merge. I falsi positivi accettati (credenziali di test in `application-test.properties` e nei manifest Kubernetes di test in `k8s/test/`) sono soppressi in modo chirurgico tramite `.trivyignore.yaml` con scope limitato ai file specifici, senza disabilitare la regola globalmente.

Trivy e Dependabot coprono superfici complementari: Dependabot aggiorna automaticamente le dipendenze dichiarate in `pom.xml`, Trivy copre anche i package OS dell'immagine base, i secrets nei file e le misconfiguration IaC — superfici che Dependabot non monitora.

---

## Analisi Statica — SonarCloud

SonarCloud analizza il codice sorgente al termine di ogni run CI riuscito, ricevendo il report JaCoCo per integrare la coverage reale.

| Metrica | Valore |
|---------|--------|
| **Reliability Rating** | A |
| **Security Rating** | A |
| **Maintainability Rating** | A |
| **Bugs** | 0 |
| **Vulnerabilities** | 0 |
| **Security Hotspots** | 0 |
| **Code Smells** | 66 |
| **Duplications** | 0.0% |
| **Coverage (SonarCloud)** | 80.7% |

I 66 code smells sono avvisi di maintainability (naming conventions, complessità ciclomatica) che non impattano correttezza o sicurezza.

> **Discrepanza di coverage:** SonarCloud riporta **80.7%** contro **82.9%** di JaCoCo. Attesa: SonarCloud può escludere classi generate automaticamente (Lombok, modelli) o calcolare su un sottoinsieme diverso di linee.