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
| `STAFF_TOKEN` | — | JWT dell'Admin o altro ruolo staff (ottenuto dopo `auth:login-staff`) |
| `GESTORE_TOKEN` | — | JWT del Gestore (ottenuto dopo `auth:login-gestore`) |
| `IMMOBILE_ID` | `1` | ID immobile usato nei test di offerta e visita |
| `OFFERTA_ID` | `1` | ID offerta usato nei test di aggiornamento stato |
| `VISITA_ID` | `1` | ID visita usato nei test di aggiornamento stato |
| `TEST_RUN_ID` | `$(date +%s)` | Suffisso univoco per email di test — permette run ripetuti |
| `CLIENT_EMAIL` | `cliente_${TEST_RUN_ID}@test.com` | Email cliente generata per ogni run |
| `STAFF_EMAIL` | `admin_manual@test.com` | Email usata da `auth:login-staff` — sovrascrivila per loggare come Gestore o Agente |
| `GESTORE_EMAIL` | `gestore_${TEST_RUN_ID}@test.com` | Email Gestore generata per ogni run |
| `AGENTE_EMAIL` | `agente_${TEST_RUN_ID}@test.com` | Email AgenteImmobiliare generata per ogni run |
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
| Password | `Password123!` |

## Flusso completo (primo utilizzo)

```bash
# 1. Seed bootstrap (una-tantum — idempotente)
./test-manual.sh seed:up

# 2. Fissa il TEST_RUN_ID per tutta la sessione — tutte le email rimangono coerenti
export TEST_RUN_ID=$(date +%s)   # bash
# set -gx TEST_RUN_ID (date +%s) # fish

# 3. Login come Admin per ottenere STAFF_TOKEN
./test-manual.sh auth:login-staff
export STAFF_TOKEN="eyJ..."      # bash
# set -gx STAFF_TOKEN "eyJ..."   # fish

# 4. Admin registra un Gestore nella propria agenzia (9999)
./test-manual.sh auth:register-gestore

# 5. Login come Gestore per ottenere GESTORE_TOKEN
./test-manual.sh auth:login-gestore
export GESTORE_TOKEN="eyJ..."    # bash
# set -gx GESTORE_TOKEN "eyJ..." # fish

# 6. Gestore registra un AgenteImmobiliare nella propria agenzia (9999)
./test-manual.sh auth:register-agente

# 7. Registra un cliente e fai login
./test-manual.sh auth:register
./test-manual.sh auth:login
export TOKEN="eyJ..."            # bash
# set -gx TOKEN "eyJ..."         # fish

# 8. Crea un immobile con il token Admin/staff
./test-manual.sh immobile:crea
export IMMOBILE_ID=<id dall'output>

# 9. Test offerte e visite
./test-manual.sh offerta:aggiungi
./test-manual.sh visita:prenota

# 10. Pulizia seed a fine sessione (opzionale)
./test-manual.sh seed:down
```

> **Importante:** esporta sempre `TEST_RUN_ID` all'inizio della sessione. Senza di esso ogni chiamata genera un timestamp diverso, producendo email differenti tra `register` e `login`.
>
> `auth:login-staff` è generico: imposta `STAFF_EMAIL` prima di chiamarlo per loggare con qualsiasi ruolo staff.
> Il default è `admin_manual@test.com` (Admin da seed). Per loggare come Gestore: `export STAFF_EMAIL="$GESTORE_EMAIL"`.

## Comandi

### `seed`

| Comando | Descrizione |
|---|---|
| `seed:up` | Inserisce nel DB via `kubectl exec`: agenzia (id=9999), Admin, link `utenteagenzia` |
| `seed:down` | Rimuove le righe del seed in ordine FK inverso |

### `auth`

| Comando | Endpoint | HTTP atteso | Note |
|---|---|---|---|
| `auth:register` | `POST /auth/register` | 200 | Registra un cliente — email univoca per `TEST_RUN_ID` |
| `auth:login` | `POST /auth/login` | 200 | Login cliente → esporta `TOKEN` |
| `auth:login-staff` | `POST /auth/login` | 200 | Login generico staff (Admin / Gestore / Agente) → esporta `STAFF_TOKEN` |
| `auth:register-gestore` | `POST /auth/register-staff` | 200 | Admin registra un Gestore — richiede `STAFF_TOKEN` (Admin) |
| `auth:login-gestore` | `POST /auth/login` | 200 | Login come Gestore → esporta `GESTORE_TOKEN` |
| `auth:register-agente` | `POST /auth/register-staff` | 200 | Gestore registra un AgenteImmobiliare — richiede `GESTORE_TOKEN` |
| `auth:login-bad` | `POST /auth/login` | 401 | Password errata — verifica il rifiuto |
| `auth` | tutti i precedenti | — | Esegue l'intera sezione in sequenza |

### `immobile`

| Comando | Endpoint | HTTP atteso | Note |
|---|---|---|---|
| `immobile:cerca` | `GET /immobile/cerca` | 200 | Ricerca pubblica per comune |
| `immobile:cerca-filtri` | `GET /immobile/cerca` | 200 | Ricerca con filtri (prezzo, bagni, tipologia) |
| `immobile:cerca-bad` | `GET /immobile/cerca` | 400 | `prezzoMin > prezzoMax` — verifica validazione |
| `immobile:crea` | `POST /immobile/crea` | 200 | Creazione immobile — richiede `STAFF_TOKEN` |
| `immobile:personali` | `GET /immobile/personali` | 200 | Lista immobili dello staff autenticato |
| `immobile` | tutti i precedenti | — | Esegue l'intera sezione in sequenza |

### `offerta`

| Comando | Endpoint | HTTP atteso | Note |
|---|---|---|---|
| `offerta:aggiungi` | `POST /offerta/aggiungi` | 200 | Nuova offerta su `IMMOBILE_ID` — richiede `TOKEN` |
| `offerta:accetta` | `PATCH /offerta/aggiorna` | 200 | Transizione → `Accettata` su `OFFERTA_ID` |
| `offerta:rifiuta` | `PATCH /offerta/aggiorna` | 200 | Transizione → `Rifiutata` su `OFFERTA_ID` |
| `offerta:riepilogo-cliente` | `GET /offerta/riepilogoCliente` | 200 | Offerte del cliente autenticato |
| `offerta:riepilogo-agenzia` | `GET /offerta/riepilogoUtenteAgenzia` | 200 | Offerte gestite dallo staff |
| `offerta` | aggiungi + riepilogo x2 | — | Esegue il sottoinsieme principale |

### `visita`

| Comando | Endpoint | HTTP atteso | Note |
|---|---|---|---|
| `visita:prenota` | `POST /visita/prenota` | 200 | Prenota su `IMMOBILE_ID` / `VISITA_DATE` — richiede `TOKEN` |
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

> I test delle sezioni `offerta` e `visita` richiedono che `TOKEN`, `STAFF_TOKEN`, `GESTORE_TOKEN` e `IMMOBILE_ID` siano già impostati prima di lanciare `all`.
