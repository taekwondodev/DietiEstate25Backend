# Manual API Tests

Script per il testing manuale degli endpoint REST via `curl` + `jq`.
Non fa parte della suite automatica — va eseguito a mano contro un'istanza in esecuzione.

## Prerequisiti

- `curl` e `jq` installati
- `kubectl` configurato sul cluster (necessario solo per `seed:up/down`)
- Server attivo (default: `http://localhost:8080`)

## Variabili d'ambiente

| Variabile | Default | Descrizione |
|---|---|---|
| `BASE_URL` | `http://localhost:8080` | URL base del server |
| `TOKEN` | — | JWT del cliente (ottenuto dopo `auth:login`) |
| `STAFF_TOKEN` | — | JWT dello staff (ottenuto dopo `auth:login-staff`) |
| `IMMOBILE_ID` | `1` | ID immobile usato nei test di offerta e visita |
| `OFFERTA_ID` | `1` | ID offerta usato nei test di aggiornamento stato |
| `VISITA_ID` | `1` | ID visita usato nei test di aggiornamento stato |
| `TEST_RUN_ID` | `$(date +%s)` | Suffisso univoco per email di test — permette run ripetuti |
| `CLIENT_EMAIL` | `cliente_${TEST_RUN_ID}@test.com` | Email cliente generata per ogni run |
| `STAFF_EMAIL` | `agente_${TEST_RUN_ID}@test.com` | Email staff generata per ogni run |
| `VISITA_DATE` | `2026-06-01` | Data usata in `visita:prenota` — cambiala se lo slot è già occupato |
| `K8S_NS` | `dietiestate25` | Namespace Kubernetes usato da `seed:up/down` |

### Run ripetuti

Ogni esecuzione genera un `TEST_RUN_ID` basato sul timestamp, quindi le email sono sempre univoche e non vanno in conflitto con i run precedenti.

Per rientrare nella stessa sessione (es. fare login dopo un register eseguito separatamente):

```bash
export TEST_RUN_ID=1744123456
```

## Seed del database

Il database di produzione gira in Kubernetes con il pod postgres raggiungibile **solo dall'interno del cluster** (Service `ClusterIP` + `NetworkPolicy` che ammette esclusivamente il pod `backend`). Non è possibile connettersi con `psql` da locale.

Non esistono endpoint API per creare agenzie o utenti Admin. Il seed usa `kubectl exec` direttamente nel pod postgres, leggendo le credenziali dal Secret `postgres-secret` in automatico.

Il seed è **idempotente**: sicuro da rieseguire più volte (`ON CONFLICT DO NOTHING`).

```bash
# Inserisce: agenzia (id=9999) + Admin (seed-admin-manual) + link utenteagenzia
./test-manual.sh seed:up

# Rimuove solo le righe inserite dal seed (in ordine FK inverso)
./test-manual.sh seed:down
```

Credenziali dell'Admin creato dal seed:

| Campo | Valore |
|---|---|
| Email | `admin_manual@test.com` |
| Password | `Test1234!` |

## Flusso completo (primo utilizzo)

```bash
# 1. Seed bootstrap (una-tantum — idempotente)
./test-manual.sh seed:up

# 2. Login come Admin per ottenere STAFF_TOKEN
export STAFF_EMAIL="admin_manual@test.com"
./test-manual.sh auth:login-staff
export STAFF_TOKEN="eyJ..."

# 3. Registra un nuovo cliente e uno staff via API
./test-manual.sh auth:register          # usa CLIENT_EMAIL generata dal TEST_RUN_ID
./test-manual.sh auth:register-staff    # crea un AgenteImmobiliare

# 4. Login cliente
./test-manual.sh auth:login
export TOKEN="eyJ..."

# 5. Crea un immobile con lo staff
./test-manual.sh immobile:crea
export IMMOBILE_ID=<id dall'output>

# 6. Test offerte e visite
./test-manual.sh offerta:aggiungi
./test-manual.sh visita:prenota

# 7. Pulizia seed a fine sessione (opzionale)
./test-manual.sh seed:down
```

## Comandi

### `seed`

| Comando | Descrizione |
|---|---|
| `seed:up` | Inserisce nel DB via `kubectl exec`: agenzia (id=9999), Admin, link `utenteagenzia` |
| `seed:down` | Rimuove le righe del seed in ordine FK inverso |

### `auth`

| Comando | Endpoint | HTTP atteso | Note |
|---|---|---|---|
| `auth:register` | `POST /auth/register` | 201 | Registra un cliente — email univoca per `TEST_RUN_ID` |
| `auth:register-staff` | `POST /auth/register-staff` | 201 | Registra uno staff — richiede `STAFF_TOKEN` |
| `auth:login` | `POST /auth/login` | 200 | Login cliente → esporta `TOKEN` |
| `auth:login-staff` | `POST /auth/login` | 200 | Login staff → esporta `STAFF_TOKEN` |
| `auth:login-bad` | `POST /auth/login` | 404 | Password errata — verifica il rifiuto |
| `auth` | tutti i precedenti | — | Esegue l'intera sezione in sequenza |

### `immobile`

| Comando | Endpoint | HTTP atteso | Note |
|---|---|---|---|
| `immobile:cerca` | `GET /immobile/cerca` | 200 | Ricerca pubblica per comune |
| `immobile:cerca-filtri` | `GET /immobile/cerca` | 200 | Ricerca con filtri (prezzo, bagni, tipologia) |
| `immobile:cerca-bad` | `GET /immobile/cerca` | 400 | `prezzoMin > prezzoMax` — verifica validazione |
| `immobile:crea` | `POST /immobile/crea` | 201 | Creazione immobile — richiede `STAFF_TOKEN` |
| `immobile:personali` | `GET /immobile/personali` | 200 | Lista immobili dello staff autenticato |
| `immobile` | tutti i precedenti | — | Esegue l'intera sezione in sequenza |

### `offerta`

| Comando | Endpoint | HTTP atteso | Note |
|---|---|---|---|
| `offerta:aggiungi` | `POST /offerta/aggiungi` | 201 | Nuova offerta su `IMMOBILE_ID` — richiede `TOKEN` |
| `offerta:accetta` | `PATCH /offerta/aggiorna` | 200 | Transizione → `Accettata` su `OFFERTA_ID` |
| `offerta:rifiuta` | `PATCH /offerta/aggiorna` | 200 | Transizione → `Rifiutata` su `OFFERTA_ID` |
| `offerta:riepilogo-cliente` | `GET /offerta/riepilogoCliente` | 200 | Offerte del cliente autenticato |
| `offerta:riepilogo-agenzia` | `GET /offerta/riepilogoUtenteAgenzia` | 200 | Offerte gestite dallo staff |
| `offerta` | aggiungi + riepilogo x2 | — | Esegue il sottoinsieme principale |

### `visita`

| Comando | Endpoint | HTTP atteso | Note |
|---|---|---|---|
| `visita:prenota` | `POST /visita/prenota` | 201 | Prenota su `IMMOBILE_ID` / `VISITA_DATE` — richiede `TOKEN` |
| `visita:prenota-bad` | `POST /visita/prenota` | 400 | Ora fuori range (22:00) — verifica validazione |
| `visita:conferma` | `PATCH /visita/aggiorna` | 200 | Transizione → `Confermata` su `VISITA_ID` |
| `visita:rifiuta` | `PATCH /visita/aggiorna` | 200 | Transizione → `Rifiutata` su `VISITA_ID` |
| `visita:riepilogo-cliente` | `GET /visita/riepilogoCliente` | 200 | Visite del cliente autenticato |
| `visita:riepilogo-agenzia` | `GET /visita/riepilogoUtenteAgenzia` | 200 | Visite gestite dallo staff |
| `visita` | prenota + prenota-bad + riepilogo x2 | — | Esegue il sottoinsieme principale |

### `geodata`

| Comando | Endpoint | HTTP atteso | Note |
|---|---|---|---|
| `geodata` | `POST /geodata` | 200 + 400 | POI intorno a Napoli; secondo caso con `raggio=0` |

### `meteo`

| Comando | Endpoint | HTTP atteso | Note |
|---|---|---|---|
| `meteo` | `POST /meteo` | 200 + 400 | Previsioni per Napoli; secondo caso con data nel passato |

## Esecuzione completa

```bash
./test-manual.sh all
```

Esegue in sequenza: `auth` → `immobile` → `offerta` → `visita` → `geodata` → `meteo`.

> I test delle sezioni `offerta` e `visita` richiedono che `TOKEN`, `STAFF_TOKEN` e `IMMOBILE_ID` siano già impostati prima di lanciare `all`.
