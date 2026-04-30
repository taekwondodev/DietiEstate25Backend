# Secrets — Gestione delle Variabili d'Ambiente Sensibili

## Variabili d'Ambiente

Tutte le variabili sono lette da [`application.properties`](../../backend/src/main/resources/application.properties). Variabile mancante → crash allo startup (Fail-Fast). Nessun valore sensibile è versionato nel repository.

| Variabile | Categoria | Obbligatoria | Descrizione |
|-----------|-----------|:------------:|-------------|
| `SPRING_DATASOURCE_URL` | Database | Sì | JDBC URL PostgreSQL (`jdbc:postgresql://host:5432/db`) |
| `SPRING_DATASOURCE_USERNAME` | Database | Sì | Utente PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | Database | Sì | Password PostgreSQL |
| `JWT_SECRET` | Auth | Sì | Secret HMAC-SHA256 — minimo 256 bit (32 byte) |
| `MAIL_HOST` | SMTP | Sì | Host del server SMTP (es. `smtp.gmail.com`) |
| `MAIL_USERNAME` | SMTP | Sì | Account mittente delle notifiche email |
| `MAIL_PASSWORD` | SMTP | Sì | Password o App Password dell'account SMTP |
| `GEO_KEY` | Geoapify | Sì | API key per servizi geospaziali e POI |
| `SERVER_PORT` | Server | No | Porta HTTP applicativa (default: `8080`) |
| `MAIL_PORT` | SMTP | No | Porta SMTP (default: `587`, STARTTLS) |

---

## Generazione del JWT Secret

Il JWT è firmato con HMAC-SHA256 — il secret deve essere di almeno 256 bit (32 byte). Generazione:

```bash
openssl rand -base64 32
```

Il valore risultante è il secret da iniettare come `JWT_SECRET`. Mai hardcoded nel codice — trattare come credenziale di produzione. Per l'utilizzo nel filter chain → [`SecurityConfig`](../../backend/src/main/java/com/dietiestate25backend/utils/SecurityConfig.java#L52).

---

## Iniezione via Kubernetes Secrets

In produzione e su cluster, le variabili sono iniettate tramite Kubernetes Secrets definiti in:
- `k8s/backend/secret.yaml` — JWT secret, datasource URL, credenziali DB, SMTP, GEO_KEY
- `k8s/postgres/secret.yaml` — credenziali PostgreSQL per il pod database

I file `secret.yaml` **non sono version controllati**. I Secret vengono montati nei pod a runtime senza mai toccare il filesystem dell'host o il repository.

L'encoding base64 è il formato richiesto da Kubernetes — non è cifratura. La protezione reale è data dal fatto che il file non è versionato e l'accesso al cluster è ristretto.

Per l'infrastruttura Kubernetes completa → [docs/k8s/README.md](../k8s/README.md).

> **Nota — scopo universitario:** l'uso di file `secret.yaml` locali è accettabile in questo contesto. In un sistema in produzione reale i secret non andrebbero mai gestiti come file statici: la soluzione corretta è un secret manager dedicato (es. **HashiCorp Vault**, AWS Secrets Manager, GCP Secret Manager) con rotazione automatica, audit log degli accessi e iniezione dinamica nei pod tramite sidecar o CSI driver — senza che il valore del secret tocchi mai il filesystem o un file versionabile.
