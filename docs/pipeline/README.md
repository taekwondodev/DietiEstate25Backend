## Pipeline

Il progetto adotta una pipeline CI strutturata su due livelli: containerizzazione con Docker per garantire ambienti riproducibili, e automazione con GitHub Actions per l'esecuzione dei test, l'analisi della qualità del codice e l'aggiornamento automatico delle dipendenze. La pipeline copre sei domini di sicurezza distinti: secrets detection (GitGuardian), SAST (SonarQube), SCA con license compliance (Snyk), DAST (OWASP ZAP), container security (Trivy) e aggiornamento automatico dipendenze (Dependabot).

### Docker

Il progetto usa due Dockerfile distinti con scopi e profili di sicurezza diversi.

#### [`Dockerfile`](../../backend/Dockerfile#L2) — produzione

**Build multi-stage** ([L2–L16](../../backend/Dockerfile#L2)): la fase di build usa `maven:3.9.13-eclipse-temurin-25` (Maven + JDK completo) per compilare e pacchettizzare. La fase di runtime parte da zero con `eclipse-temurin:25-jre-alpine` e copia solo il JAR compilato. Maven, il JDK, i sorgenti, i file di test e le dipendenze di build non entrano nell'immagine finale — eliminando una classe intera di strumenti che un attaccante potrebbe sfruttare per compilare o eseguire codice arbitrario.

**JRE Alpine** ([L16](../../backend/Dockerfile#L16)): Alpine Linux ha una superficie OS drasticamente ridotta rispetto a Debian o Ubuntu — meno package installati significa meno CVE potenziali da gestire. Combinato con il solo JRE (senza `javac`, `jshell`, strumenti di debug), l'immagine espone solo ciò che è strettamente necessario all'esecuzione.

**Aggiornamento proattivo dei package** ([L20–21](../../backend/Dockerfile#L20)): `apk upgrade --no-cache` aggiorna tutti i package OS a ogni build, patchando CVE noti nell'immagine base senza aspettare un aggiornamento upstream. Il parametro `BUILD_WEEK` invalida questo layer settimanalmente in CI: senza questo meccanismo, Docker riutilizzerebbe il layer dalla cache indefinitamente, rendendo l'upgrade di fatto un no-op tra build ravvicinate.

**Utente non privilegiato** ([L24–33](../../backend/Dockerfile#L24)): l'applicazione gira come `appuser` (sistema, no-login) nel gruppo `appgroup`. Un processo root dentro un container, in caso di escape, ottiene privilegi root sull'host — un processo non-root riduce drasticamente l'impatto di una compromissione. Il JAR viene copiato con `--chown=appuser:appgroup` per garantire che l'utente possa leggerlo senza permessi aggiuntivi.

**Nessun secret nell'immagine**: nessuna variabile d'ambiente, credenziale o configurazione sensibile è hardcoded nel Dockerfile. Tutti i valori runtime (datasource, JWT secret, SMTP) sono iniettati dall'esterno — via Kubernetes Secrets in produzione, via Docker Compose in test.

#### [`Dockerfile.test`](../../backend/Dockerfile.test#L3) — test

Immagine a singolo stage basata su `maven:3.9.13-eclipse-temurin-25`: esegue `mvn clean test` e termina. Non applica le stesse restrizioni del Dockerfile di produzione (nessun utente non-root, nessun upgrade OS) perché il suo ciclo di vita è effimero — viene creata, usata per la durata dei test e distrutta. Non viene mai pubblicata su alcun registry esterno.

### GitHub Actions

La configurazione di GitHub Actions è composta da sei file in `.github/`. Un orchestratore centrale [`ci.yml`](../../.github/workflows/ci.yml#L3) chiama quattro workflow in sequenza; il deploy è separato e si attiva al completamento di CI:

```
push / pull_request
        │
        ▼
       CI ──────────────────────────────────────────── (workflow_run on CI success)
        ├── 1. secrets → GitGuardian (secrets scan)              │
        ├── 2. test    → build + test + JaCoCo                   │
        ├── 3. sast    → SonarQube        ┐ (dopo test)          │
        ├── 4. sca     → Snyk             ┘ (dopo test)          │
        ├── 5. dast    → OWASP ZAP (dopo sast + sca)             │
        └── 6. trivy   → image scan (dopo dast)                 ▼
                                                           Deploy → Docker Hub
```

Il deploy viene eseguito solo se **tutti** gli step di CI completano con successo. Nella dashboard di GitHub Actions appaiono **due pipeline distinte**: **CI** e **Deploy**. I workflow [`gitguardian.yml`](../../.github/workflows/gitguardian.yml#L1), [`test.yml`](../../.github/workflows/test.yml#L4), [`sonar.yml`](../../.github/workflows/sonar.yml#L4), [`snyk.yml`](../../.github/workflows/snyk.yml#L1), [`zap.yml`](../../.github/workflows/zap.yml#L4) e [`trivy.yml`](../../.github/workflows/trivy.yml#L3) sono privi di trigger autonomi e vengono eseguiti come reusable workflow orchestrati da [`ci.yml`](../../.github/workflows/ci.yml#L3); [`deploy.yml`](../../.github/workflows/deploy.yml#L4) è invece autonomo con trigger `workflow_run` su `CI`.

**[`ci.yml`](../../.github/workflows/ci.yml#L3)** — orchestratore della pipeline di verifica. Si attiva ad ogni push sul branch `security` e ad ogni pull request. Coordina sei workflow riutilizzabili in un grafo di dipendenze parzialmente parallelo: `secrets` (GitGuardian) e `test` partono in parallelo senza dipendenze; `sast` (SonarQube) e `sca` (Snyk) partono dopo `test` in parallelo tra loro; `dast` (ZAP) parte solo dopo che entrambi `sast` e `sca` completano; `trivy` (image scan) segue `dast`.

#### [`test.yml`](../../.github/workflows/test.yml#L4)

Reusable workflow (`workflow_call`), chiamato da [`ci.yml`](../../.github/workflows/ci.yml#L3). Esegue i seguenti step:

1. Build dell'immagine Docker di test con **Docker BuildKit**, sfruttando la cache dei layer su GitHub Actions: se [`pom.xml`](../../backend/pom.xml#L1) e [`Dockerfile.test`](../../backend/Dockerfile.test#L3) non sono cambiati, il layer con le dipendenze Maven viene ripristinato dalla cache, evitando di riscaricarlo ad ogni run.
2. Esecuzione dei test tramite `docker compose up`. Al termine, il report di coverage generato da JaCoCo viene estratto dal container con `docker compose cp`, evitando conflitti con `mvn clean` che non può eliminare una directory montata come volume.
3. Upload del report `jacoco.xml` come artifact temporaneo (retention 1 giorno), reso disponibile al workflow successivo.

**Output:** artifact [`jacoco.xml`](https://github.com/taekwondodev/DietiEstate25Backend/actions/workflows/ci.yml) passato a [`sonar.yml`](../../.github/workflows/sonar.yml#L4) nella stessa workflow run. Non pubblicato esternamente — scade dopo 1 giorno *(il link rimanda alla pagina delle run CI; il download diretto potrebbe non essere attivo se l'artifact è scaduto)*.

#### [`gitguardian.yml`](../../.github/workflows/gitguardian.yml#L1)

Reusable workflow (`workflow_call`), chiamato da [`ci.yml`](../../.github/workflows/ci.yml#L3) in parallelo con `test` — non richiede build né compilazione. Usa l'action ufficiale `gitguardian/gg-shield-action` per scansionare il repository alla ricerca di **secrets hardcodati**: API key, token di accesso, credenziali database, certificati privati e qualsiasi stringa che corrisponda ai pattern di oltre 400 provider noti. La scansione copre sia i file presenti nel commit corrente sia, in modalità PR, tutti i commit aggiunti dal branch — garantendo che un secret introdotto in un commit intermedio non sfugga anche se rimosso in un commit successivo.

A differenza di Trivy (che segnala credenziali solo come parte di una scansione CVE su file statici) e di SonarQube (che cerca pattern generici di hardcoded credentials), GitGuardian è specializzato esclusivamente nel secrets detection: mantiene un database continuamente aggiornato di pattern per provider specifici (AWS, GCP, GitHub, database, servizi di terze parti) con tasso di falsi positivi molto basso.

Il workflow fallisce (`exit-code: 1`) se viene rilevato un secret non ignorato, bloccando il merge prima ancora che la build sia completata.

**Output:** finding riportati direttamente nel log del job con tipo di secret, file e riga. Richiede il secret `GITGUARDIAN_API_KEY` configurato nel repository.

#### [`sonar.yml`](../../.github/workflows/sonar.yml#L4)

Reusable workflow (`workflow_call`), chiamato da [`ci.yml`](../../.github/workflows/ci.yml#L3) dopo `test`. Scarica il report JaCoCo prodotto nella stessa run, compila i sorgenti con Maven e lancia l'analisi SonarQube con coverage reale (non disponibile senza database PostgreSQL).

**Output:** analisi pubblicata su [SonarCloud](https://sonarcloud.io/project/overview?id=taekwondodev_DietiEstate25Backend). Risultati attuali:

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

I 66 code smells sono avvisi di maintainability (naming conventions, complessità ciclomatica) che non impattano correttezza o sicurezza — accettati senza intervento.

> **Discrepanza di coverage:** SonarCloud riporta **80.7%** contro **82.9%** di JaCoCo. SonarCloud esclude classi generate automaticamente (Lombok, modelli) o calcola su un sottoinsieme diverso di linee — atteso e non actionable.

#### [`snyk.yml`](../../.github/workflows/snyk.yml#L1)

Reusable workflow (`workflow_call`), chiamato da [`ci.yml`](../../.github/workflows/ci.yml#L3) in parallelo con `sonar` dopo il completamento di `test`. Esegue due job distinti:

1. **Open Source / SCA** (`snyk-oss`): scansiona [`pom.xml`](../../backend/pom.xml#L1) con `snyk test` — confronta ogni dipendenza Maven contro il database Snyk di vulnerabilità note. A differenza di Trivy (che riporta CVE senza indicare una correzione praticabile), Snyk fornisce per ogni finding: il percorso di dipendenza transitiva che introduce la vulnerabilità, la versione corretta disponibile e, dove possibile, un fix automatico via PR. Fallisce in presenza di vulnerabilità HIGH o CRITICAL senza fix disponibile.

2. **License Compliance** (`snyk-license`): esegue `snyk test --print-deps` con policy configurata in [`.snyk`](../../.snyk#L1) per bloccare licenze incompatibili con il progetto (GPL-2.0, GPL-3.0, AGPL-3.0). Questo controllo non è coperto né da Trivy né da SonarQube — entrambi operano su CVE, non su license metadata.

**Separazione dei domini rispetto a Trivy:** Trivy image scan rimane responsabile dei CVE nei package OS dell'immagine base (`eclipse-temurin:25-jre-alpine`) — superficie che Snyk non copre per default con `snyk test`. Snyk copre le dipendenze Maven (incluse le transitive) con remediation advice e la compliance delle licenze. Il job `trivy-fs` è stato rimosso per eliminare la ridondanza sulla scansione del `pom.xml`.

**Output:** finding riportati nel log del job con severità, CVE ID e fix consigliato. Richiede il secret `SNYK_TOKEN` configurato nel repository.

#### [`zap.yml`](../../.github/workflows/zap.yml#L4)

Reusable workflow (`workflow_call`), richiamato da [`ci.yml`](../../.github/workflows/ci.yml#L3). Esegue un singolo job (`zap`) che condivide setup e teardown tra tutte le scansioni:

1. Build dell'immagine di produzione con Docker BuildKit (cache condivisa con la chiave `buildx-dast-*`).
2. Avvio di PostgreSQL e MailHog (SMTP mock) tramite [`compose.dast.yaml`](../../compose.dast.yaml#L1), con tutte le variabili d'ambiente necessarie iniettate direttamente nel container. Il DB viene inizializzato con le fixture di test in [`02_test_data.sql`](../../db-init/02_test_data.sql#L28), che includono utenti precostituiti per ogni ruolo.
3. **Baseline Scan**: scansione **passiva** con [OWASP ZAP](https://www.zaproxy.org/) — intercetta e analizza il traffico HTTP senza inviare payload aggressivi. Individua vulnerabilità di configurazione, header di sicurezza mancanti e informazioni esposte. Tipicamente completa in 2–5 minuti.
4. **API Scan (Cliente / AgenteImmobiliare / Admin)**: tre scansioni **attive** sequenziali, ciascuna autenticata con un ruolo diverso. ZAP legge [`openapi.yaml`](../../backend/openapi.yaml#L1) per scoprire tutti gli endpoint definiti nell'API — anziché affidarsi allo spider — garantendo coverage completa anche sugli endpoint protetti da JWT che risponderebbero altrimenti con `401`. Prima di ogni scan, uno step dedicato effettua il login tramite `POST /auth/login` con le credenziali del ruolo corrispondente, maschera il token JWT nei log con `::add-mask::` e lo inietta in tutte le richieste ZAP tramite il Replacer add-on (`Authorization: Bearer <token>`). In questo modo ZAP raggiunge e testa gli endpoint protetti da RBAC che sarebbero altrimenti irraggiungibili. I finding comuni a più ruoli emergono in più report, aumentando la priorità percepita. Ogni API scan invia payload di attacco reali (SQLi, XSS, path traversal, CSRF, header injection, ecc.) verso ogni endpoint per verificare se l'applicazione risponde in modo vulnerabile.
5. Tear down dell'ambiente con `-v`, eseguito sempre indipendentemente dall'esito.

**Output:** ogni scansione pubblica i risultati in una **GitHub Issue** dedicata (creata o aggiornata ad ogni run): [ZAP Baseline](https://github.com/taekwondodev/DietiEstate25Backend/issues?q=is%3Aissue+ZAP+Baseline), [ZAP API Scan – Cliente](https://github.com/taekwondodev/DietiEstate25Backend/issues?q=is%3Aissue+ZAP+API+Cliente), [ZAP API Scan – AgenteImmobiliare](https://github.com/taekwondodev/DietiEstate25Backend/issues?q=is%3Aissue+ZAP+API+AgenteImmobiliare), [ZAP API Scan – Admin](https://github.com/taekwondodev/DietiEstate25Backend/issues?q=is%3Aissue+ZAP+API+Admin). I report HTML/JSON sono archiviati come artifact separati nella [CI workflow page](https://github.com/taekwondodev/DietiEstate25Backend/actions/workflows/ci.yml) (`zap-baseline-report`, `zap-api-scan-cliente`, `zap-api-scan-agente`, `zap-api-scan-admin`) — scadono dopo 90 giorni *(il link rimanda alla pagina delle run CI; il download diretto potrebbe non essere attivo se l'artifact è scaduto)*.

**Finding gestiti:**

Regola soppressa tramite [`.zap/rules.tsv`](../../.zap/rules.tsv#L1) in tutte le scansioni:
- `10049` — Non-Storable Content: `Cache-Control: no-store` è il comportamento corretto per un backend API stateless, non una vulnerabilità.

Vulnerabilità corretta:
- `40042` — Spring Actuator Health (Medium): ZAP rilevava `/actuator/health` esposto sulla porta applicativa 8080, raggiungibile tramite il `Service` NodePort insieme al resto delle API. **Prima:** l'endpoint di health era accessibile da chiunque raggiungesse il cluster sulla porta 8080, nella stessa superficie esposta all'esterno. **Soluzione:** `management.server.port=8081` in [`application.properties`](../../backend/src/main/resources/application.properties#L17) separa il management server dalla porta applicativa. **Dopo:** il Kubernetes readiness probe punta a 8081 ([`deployment.yaml`](../../k8s/backend/deployment.yaml#L43)); la porta 8081 non è dichiarata nel `Service` NodePort, quindi non è raggiungibile dall'esterno del cluster — solo dal piano di controllo Kubernetes per i probe interni.

#### [`trivy.yml`](../../.github/workflows/trivy.yml#L3)

Reusable workflow (`workflow_call`), chiamato da [`ci.yml`](../../.github/workflows/ci.yml#L3) dopo `dast`. Esegue un singolo job focalizzato esclusivamente sulla **sicurezza del container**:

- **Image Scan** (`trivy-image`): builda l'immagine di produzione ([`Dockerfile`](../../backend/Dockerfile#L2)) e scansiona i package OS del layer runtime (`eclipse-temurin:25-jre-alpine`) — librerie di sistema, glibc, OpenSSL e qualsiasi package Alpine presente nell'immagine finale. Sfrutta la stessa strategia di cache Docker BuildKit usata in [`test.yml`](../../.github/workflows/test.yml#L4), con una chiave separata per evitare collisioni. Il job fallisce con `exit-code: 1` in presenza di CVE HIGH o CRITICAL con fix disponibile, bloccando il merge.

Il job `trivy-fs` (scansione del filesystem e di `pom.xml`) è stato rimosso perché la sua responsabilità è ora suddivisa tra strumenti specializzati: GitGuardian copre i secrets nei file, Snyk copre le dipendenze Maven con remediation advice. Trivy rimane l'unico responsabile della superficie OS dell'immagine Docker — superficie non coperta né da Snyk né da GitGuardian.

**Output:** risultati pubblicati nel tab [Security → Code scanning alerts](https://github.com/taekwondodev/DietiEstate25Backend/security/code-scanning) di GitHub in formato SARIF sotto la categoria `trivy-image`.

**Finding gestiti:**

Vulnerabilità di librerie di sistema corrette:
Trivy Image Scan ha riportato CVE HIGH su package OS dell'immagine base `eclipse-temurin:25-jre-alpine` (librerie di sistema non aggiornate nello strato fornito dall'upstream). **Prima:** l'immagine runtime ereditava i package nella versione frozen dell'immagine base, con CVE noti presenti al momento della build. **Soluzione:** `RUN apk upgrade --no-cache` nello stage runtime del [`Dockerfile`](../../backend/Dockerfile#L21) aggiorna tutti i package OS a ogni build, applicando le patch disponibili indipendentemente dalla versione dell'immagine base. L'`ARG BUILD_WEEK` ([L20](../../backend/Dockerfile#L20)) invalida questo layer settimanalmente in CI — senza di esso Docker riutilizzerebbe il layer dalla cache, rendendo l'upgrade un no-op tra build ravvicinate. **Dopo:** le scan successive non riportano più CVE HIGH/CRITICAL con fix disponibile sui package di sistema.

#### [`deploy.yml`](../../.github/workflows/deploy.yml#L4)

Si attiva tramite `workflow_run` al completamento con successo di `CI`. Contiene un singolo job che builda l'immagine di produzione con Docker BuildKit e la pubblica su Docker Hub con due tag: `latest` e il SHA del commit (`taekwondodev/dietiestate25-backend:<sha>`), garantendo tracciabilità e possibilità di rollback. Richiede i secret `DOCKERHUB_USERNAME` e `DOCKERHUB_TOKEN` configurati nel repository.

**Output:** immagine pubblicata su [Docker Hub](https://hub.docker.com/r/taekwondodev/dietiestate25-backend) con tag `latest` e `<commit-sha>`. Il tag SHA permette rollback deterministico a qualsiasi build precedente.

#### [`dependabot.yml`](../../.github/dependabot.yml#L4)

Configura Dependabot per il monitoraggio automatico delle dipendenze su tre ecosistemi:

- **Maven** — controlla [`pom.xml`](../../backend/pom.xml#L1) per aggiornamenti alle dipendenze Java/Spring Boot.
- **Docker** — controlla le base image in [`Dockerfile`](../../backend/Dockerfile#L2) e [`Dockerfile.test`](../../backend/Dockerfile.test#L3) per nuove versioni.
- **GitHub Actions** — controlla le versioni delle action usate nei workflow (es. `actions/checkout`, `actions/cache`).

Il primo di ogni mese Dependabot apre automaticamente PR separate per ogni aggiornamento disponibile. Le PR passano attraverso l'intera pipeline CI ([`ci.yml`](../../.github/workflows/ci.yml#L3)) prima del merge, garantendo che nessun aggiornamento rompa la build. Dependabot gestisce anche gli **aggiornamenti di sicurezza** in modo autonomo, aprendo PR urgenti in caso di vulnerabilità note indipendentemente dallo schedule mensile.

**Output:** PR automatiche su GitHub, ciascuna associata a un diff di versione e ai risultati CI prima del merge.
