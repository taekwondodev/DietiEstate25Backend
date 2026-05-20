## Pipeline

Pipeline CI su due livelli: containerizzazione Docker per ambienti riproducibili, GitHub Actions per test, analisi sicurezza e aggiornamento dipendenze. Sette domini di sicurezza coperti da tool distinti e non sovrapposti.

### Overview

| Strumento | Dominio | Finding | Status |
|-----------|---------|---------|--------|
| [Test Suite](#testyml) | Security testing (OWASP WSTG) | [docs/testing/](../testing/README.md) | Pass |
| [GitGuardian](#gitguardianymL) | Secrets detection | 10 rilevati — soppressi con giustificazione | Pass |
| [SonarQube](#sonaryml) | SAST dataflow | 0 | Pass |
| [Semgrep](#semgrepyml) | SAST pattern-based | 0 | Pass |
| [Snyk](#snykyml) | SCA + license compliance | 3 batch CVE — corretti | Pass |
| [OWASP ZAP](#zapyml) | DAST | 1 corretto, 1 soppresso | Pass |
| [Trivy](#trivyyml) | Container security | CVE HIGH su package Alpine OS layer — corretti | Pass |
| [Docker Scout](#docker-scout) | Post-deploy CVE | 3 Medium — accettati (no fix disponibile) | Pass |
| [Dependabot](#dependabotyml) | Deps aggiornamento | 9 PR automatiche mergeate | Merged |

---

### Docker

Il progetto usa due Dockerfile distinti con scopi e profili di sicurezza diversi.

#### [`Dockerfile`](../../backend/Dockerfile#L2) — produzione

Build **multi-stage** ([L2–L16](../../backend/Dockerfile#L2)):

- **Stage build:** `maven:3.9.13-eclipse-temurin-25` — compila e pacchettizza
- **Stage runtime:** `eclipse-temurin:25-jre-alpine` — copia solo il JAR; Maven, JDK, sorgenti e dipendenze di build esclusi dall'immagine finale
- **Alpine + solo JRE** ([L16](../../backend/Dockerfile#L16)): superficie OS minima, nessun `javac`/`jshell`/strumenti di debug
- **`apk upgrade --no-cache`** ([L20–21](../../backend/Dockerfile#L20)): patcha CVE OS a ogni build; `ARG BUILD_WEEK` invalida il layer settimanalmente in CI (senza: Docker riutilizza cache → upgrade no-op)
- **Utente non privilegiato** ([L24–33](../../backend/Dockerfile#L24)): `appuser` (sistema, no-login); root in container = root su host in caso di escape
- **Nessun secret hardcoded**: tutti i valori runtime iniettati esternamente (Kubernetes Secrets in prod, Docker Compose in test)

#### [`Dockerfile.test`](../../backend/Dockerfile.test#L3) — test

Single-stage su `maven:3.9.13-eclipse-temurin-25`: esegue `mvn clean test` e termina. Ciclo di vita effimero — mai pubblicata su registry esterno; restrizioni di produzione non applicabili.

---

### GitHub Actions

[`ci.yml`](../../.github/workflows/ci.yml#L3) orchestra sette workflow riutilizzabili in un grafo sequenziale-parallelo. [`deploy.yml`](../../.github/workflows/deploy.yml#L4) è autonomo con trigger `workflow_run` su CI completata con successo.

```
push / pull_request
        │
        ▼
       CI ──────────────────────────────────────────────────── (workflow_run on CI success)
        ├── 1. secrets → GitGuardian                                      │
        ├── 2. test    → build + test + JaCoCo (dopo secrets)             │
        ├── 3. sast    → SonarCloud        ┐                              │
        ├── 4. semgrep → Semgrep           ┤ (dopo test, in parallelo)    │
        ├── 5. sca     → Snyk             ┘                              │
        ├── 6. dast    → OWASP ZAP   ┐ (dopo sast+semgrep+sca)           │
        └── 7. trivy   → image scan  ┘ (in parallelo)                    ▼
                                                               Deploy → Docker Hub
```

Il deploy parte solo se **tutti** gli step CI completano con successo.

---

#### [`gitguardian.yml`](../../.github/workflows/gitguardian.yml#L1)

Scansione **globale** dell'intera git history (`fetch-depth: 0`) con [`ggshield secret scan repo`](https://github.com/GitGuardian/ggshield). Rileva secrets hardcodati su 500+ provider noti. Parte prima di qualsiasi build — un secret rilevato blocca tutto il resto. Finding pubblicati in [Security → Code scanning alerts](https://github.com/taekwondodev/DietiEstate25Backend/security/code-scanning) (categoria `gitguardian`).

**Finding: 10 incident**

| Categoria | Azione |
|-----------|--------|
| Falsi positivi su dati di test | Soppressi in [`.gitguardian.yaml`](../../.gitguardian.yaml#L1) |
| Secret di test environment committati intenzionalmente | Soppressi in [`.gitguardian.yaml`](../../.gitguardian.yaml#L1) |
| Secret storici rimossi e invalidati | Soppressi in [`.gitguardian.yaml`](../../.gitguardian.yaml#L1) |

Ogni soppressione documentata con giustificazione nel file di configurazione.

---

#### [`test.yml`](../../.github/workflows/test.yml#L4)

Build immagine di test con Docker BuildKit (cache layer su [`pom.xml`](../../backend/pom.xml#L1) + [`Dockerfile.test`](../../backend/Dockerfile.test#L3)), esecuzione via `docker compose up`, estrazione report JaCoCo con `docker compose cp`. Artifact `jacoco.xml` passato a `sonar.yml` nella stessa run (retention 1 giorno).

Esegue 344 security test distribuiti su 5 aree OWASP WSTG — finding e bug corretti in [docs/testing/](../testing/README.md).

**Output:** [artifact `jacoco.xml`](https://github.com/taekwondodev/DietiEstate25Backend/actions/workflows/ci.yml) passato a `sonar.yml` (scade dopo 1 giorno).

---

#### [`sonar.yml`](../../.github/workflows/sonar.yml#L4)

SAST dataflow con taint tracking. Scarica il report JaCoCo, compila con Maven, lancia analisi SonarCloud con `-Dsonar.qualitygate.wait=true` — il job blocca il merge se il quality gate non passa. Gate configurato su sole quattro condizioni di sicurezza (metriche di qualità escluse).

**Finding: nessuno.**

| Metrica quality gate | Valore |
|----------------------|--------|
| Security Rating | A |
| Bugs | 0 |
| Vulnerabilities | 0 |
| Security Hotspots Reviewed | 0 |

Output: analisi su [SonarCloud](https://sonarcloud.io/project/overview?id=taekwondodev_DietiEstate25Backend).

---

#### [`semgrep.yml`](../../.github/workflows/semgrep.yml#L1)

SAST pattern-based su AST con due ruleset ufficiali:
- **`p/java`** — deserializzazione, SSRF, path traversal, injection (`Runtime.exec`, `ProcessBuilder`, JDBC, JNDI)
- **`p/owasp-top-ten`** — mapping OWASP A01–A10

Complementare a SonarQube: engine diversi su superfici parzialmente sovrapposte. `continue-on-error: true` — finding non bloccano ma vengono pubblicati in [Code scanning alerts](https://github.com/taekwondodev/DietiEstate25Backend/security/code-scanning) (categoria `semgrep`).

**Finding: nessuno.** 57 file Java scansionati, 65 regole attive (60 `p/java` + 5 `<multilang>`), 0 risultati.

---

#### [`snyk.yml`](../../.github/workflows/snyk.yml#L1)

Due job distinti: **SCA** (`snyk test --fail-on=upgradable` su [`pom.xml`](../../backend/pom.xml#L1)) e **License Compliance** (`snyk test --print-deps` con policy in [`.snyk`](../../.snyk#L1) che blocca GPL-2.0/3.0, AGPL-3.0). Snyk copre dipendenze Maven transitive + remediation advice; Trivy copre il layer OS. Finding pubblicati in [Code scanning alerts](https://github.com/taekwondodev/DietiEstate25Backend/security/code-scanning) (categorie `snyk-oss`, `snyk-license`).

**Finding: 3 batch di CVE corretti, 0 violazioni di licenza.**

| Componente | Severità | Versione vulnerabile | Fix applicato |
|------------|----------|----------------------|---------------|
| Spring Boot + Spring Security | 1 Critical, 4 High | 4.0.5 / 7.0.4 | → 4.0.6 / 7.0.5 in [`pom.xml`](../../backend/pom.xml#L8) |
| `org.postgresql:postgresql` ([SNYK-JAVA-ORGPOSTGRESQL-16321668](https://security.snyk.io/vuln/SNYK-JAVA-ORGPOSTGRESQL-16321668)) | High | 42.7.10 (BOM Spring Boot 4.0.6) | override `<postgresql.version>42.7.11</postgresql.version>` in [`pom.xml`](../../backend/pom.xml#L20) |
| `tomcat-embed-core` / `tomcat-embed-websocket` | 1 Critical, 1 High, 5 Medium | 11.0.21 | override `<tomcat.version>11.0.22</tomcat.version>` in [`pom.xml`](../../backend/pom.xml#L20) |

<details>
<summary>CVE Tomcat 11.0.21 → 11.0.22 (dettaglio)</summary>

| CVE | Severità | Descrizione |
|-----|----------|-------------|
| [CVE-2026-43512](https://nvd.nist.gov/vuln/detail/CVE-2026-43512) | Critical | Digest Authenticator autentica utenti sconosciuti con password `null` |
| [CVE-2026-41284](https://nvd.nist.gov/vuln/detail/CVE-2026-41284) | High | DoS via lettura illimitata in WebDAV LOCK/PROPFIND |
| [CVE-2026-42498](https://nvd.nist.gov/vuln/detail/CVE-2026-42498) | Medium | Esposizione header autenticazione WebSocket dopo redirect |
| [CVE-2026-43513](https://nvd.nist.gov/vuln/detail/CVE-2026-43513) | Medium | LockOutRealm case-sensitive sui nomi utente — riduce protezione brute-force |
| [CVE-2026-41293](https://nvd.nist.gov/vuln/detail/CVE-2026-41293) | Medium | Header HTTP/2 non validati |
| [CVE-2026-43515](https://nvd.nist.gov/vuln/detail/CVE-2026-43515) | Medium | Security constraint non applicato con vincoli multipli sullo stesso pattern |
| [CVE-2026-43514](https://nvd.nist.gov/vuln/detail/CVE-2026-43514) | Medium | Confronto segreto AJP non in tempo costante (timing attack) |

</details>

---

#### [`zap.yml`](../../.github/workflows/zap.yml#L4)

DAST con quattro scansioni sequenziali sullo stesso ambiente (PostgreSQL + MailHog via [`compose.dast.yaml`](../../compose.dast.yaml#L1), fixture [`02_test_data.sql`](../../db-init/02_test_data.sql#L28)):

1. **Baseline Scan** — scansione passiva; intercetta traffico, rileva header mancanti e configurazioni errate
2. **API Scan × 3 ruoli** (Cliente / AgenteImmobiliare / Admin) — scansione attiva guidata da [`openapi.yaml`](../../backend/openapi.yaml#L1); ogni run fa login, maschera il JWT con `::add-mask::` e lo inietta via Replacer add-on per raggiungere endpoint RBAC-protetti; payload: SQLi, XSS, path traversal, CSRF, header injection

Finding pubblicati come **GitHub Issue** dedicate: [ZAP Baseline](https://github.com/taekwondodev/DietiEstate25Backend/issues?q=is%3Aissue+ZAP+Baseline) · [Cliente](https://github.com/taekwondodev/DietiEstate25Backend/issues?q=is%3Aissue+ZAP+API+Cliente) · [AgenteImmobiliare](https://github.com/taekwondodev/DietiEstate25Backend/issues?q=is%3Aissue+ZAP+API+AgenteImmobiliare) · [Admin](https://github.com/taekwondodev/DietiEstate25Backend/issues?q=is%3Aissue+ZAP+API+Admin). Report HTML/JSON come artifact (90 giorni) nella [CI workflow page](https://github.com/taekwondodev/DietiEstate25Backend/actions/workflows/ci.yml).

**Finding: 1 corretto, 1 soppresso.**

| ID | Finding | Severità | Azione |
|----|---------|----------|--------|
| `40042` | Spring Actuator Health esposto su porta applicativa | Medium | **Corretto** |
| `10049` | Non-Storable Content (`Cache-Control: no-store`) | Info | **Soppresso** — comportamento corretto per API stateless |

**Fix `40042`:**

| | Dettaglio |
|-|-----------|
| **Prima** | `/actuator/health` raggiungibile sulla porta 8080, stessa superficie esposta all'esterno del cluster |
| **Soluzione** | `management.server.port=8081` in [`application.properties`](../../backend/src/main/resources/application.properties#L17) |
| **Dopo** | Readiness probe Kubernetes punta a 8081 ([`deployment.yaml`](../../k8s/backend/deployment.yaml#L43)); porta 8081 non dichiarata nel `Service` NodePort — irraggiungibile dall'esterno |

---

#### [`trivy.yml`](../../.github/workflows/trivy.yml#L3)

Image scan sulla superficie OS dell'immagine di produzione ([`Dockerfile`](../../backend/Dockerfile#L2)): package Alpine, glibc, OpenSSL confrontati contro NVD/OSV/GitHub Advisory. Fallisce con `exit-code: 1` su CVE HIGH/CRITICAL con fix disponibile. Unico strumento della pipeline responsabile del layer OS — né Snyk né SonarQube/Semgrep coprono questa superficie. Finding pubblicati in [Code scanning alerts](https://github.com/taekwondodev/DietiEstate25Backend/security/code-scanning) (categoria `trivy-image`).

**Finding: CVE HIGH su package OS — corretti.**

| | Dettaglio |
|-|-----------|
| **Prima** | Package Alpine frozen nell'immagine base `eclipse-temurin:25-jre-alpine`; CVE HIGH presenti al momento della build |
| **Soluzione** | `RUN apk upgrade --no-cache` nello stage runtime ([`Dockerfile`](../../backend/Dockerfile#L21)); `ARG BUILD_WEEK` ([L20](../../backend/Dockerfile#L20)) invalida il layer settimanalmente in CI |
| **Dopo** | 0 CVE HIGH/CRITICAL con fix disponibile nelle scan successive |

---

#### Docker Scout

Analisi continua post-deploy sull'immagine `taekwondodev/dietiestate25-backend` su Docker Hub, complementare a Trivy.

**Finding: 3 Medium accettati (nessun fix disponibile su Alpine).**

| CVE | Package | Motivo accettazione |
|-----|---------|---------------------|
| CVE-2025-60876 | `busybox 1.37.0-r30` | Nessun fix disponibile su Alpine |
| CVE-2016-2781 | `coreutils 9.8-r1` | Nessun fix disponibile su Alpine |
| CVE-2026-23865 | `freetype 2.14.1-r0` | Nessun fix disponibile su Alpine |

Alternativa: sostituire immagine base con distroless/scratch. Complessità di build sproporzionata rispetto al rischio effettivo.

---

#### [`deploy.yml`](../../.github/workflows/deploy.yml#L4)

Si attiva tramite `workflow_run` al completamento con successo di CI. Build immagine di produzione con Docker BuildKit, push su Docker Hub con due tag: `latest` e SHA commit (`taekwondodev/dietiestate25-backend:<sha>`). Il tag SHA garantisce tracciabilità e rollback deterministico a qualsiasi build precedente. Richiede `DOCKERHUB_USERNAME` e `DOCKERHUB_TOKEN` nei secret del repository.

**Output:** immagine su [Docker Hub](https://hub.docker.com/r/taekwondodev/dietiestate25-backend).

---

#### [`dependabot.yml`](../../.github/dependabot.yml#L4)

Monitoraggio automatico su tre ecosistemi: **Maven** ([`pom.xml`](../../backend/pom.xml#L1)), **Docker** (immagini base), **GitHub Actions** (versioni delle action). PR aperte il primo di ogni mese; PR urgenti in caso di vulnerabilità note indipendentemente dallo schedule. Ogni PR passa attraverso l'intera pipeline CI prima del merge.

> **Nota secrets:** GitHub tratta i workflow Dependabot come fork — i repository secrets sono inaccessibili. `GITGUARDIAN_API_KEY`, `SNYK_TOKEN`, `SONAR_TOKEN`, `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN` devono essere configurati anche in Settings → Security → Secrets → **Dependabot**.

**Finding: 9 PR automatiche mergeate.**

| PR | Ecosistema | Aggiornamento |
|----|------------|---------------|
| [#3](https://github.com/taekwondodev/DietiEstate25Backend/pull/3) | GitHub Actions | `actions/download-artifact` 7 → 8 |
| [#4](https://github.com/taekwondodev/DietiEstate25Backend/pull/4) | Maven | `jacoco-maven-plugin` 0.8.12 → 0.8.14 |
| [#5](https://github.com/taekwondodev/DietiEstate25Backend/pull/5) | Maven | `spring-security-oauth2-jose` → 7.0.4 |
| [#6](https://github.com/taekwondodev/DietiEstate25Backend/pull/6) | Docker | `maven` → `3.9.13-eclipse-temurin-25` (Dockerfile.test) |
| [#7](https://github.com/taekwondodev/DietiEstate25Backend/pull/7) | Maven | `spring-boot-starter-parent` 3.4.1 → 4.0.5 |
| [#8](https://github.com/taekwondodev/DietiEstate25Backend/pull/8) | Docker | `eclipse-temurin` 21-jre-jammy → 25-jre-jammy |
| [#16](https://github.com/taekwondodev/DietiEstate25Backend/pull/16) | GitHub Actions | `aquasecurity/trivy-action` 0.35.0 → 0.36.0 |
| [#17](https://github.com/taekwondodev/DietiEstate25Backend/pull/17) | GitHub Actions | `docker/login-action` 3 → 4 |
| [#18](https://github.com/taekwondodev/DietiEstate25Backend/pull/18) | Docker | `maven` 3.9.13 → 3.9.14-eclipse-temurin-25 (Dockerfile.test) |
