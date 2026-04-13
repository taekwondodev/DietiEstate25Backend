# Manual API Tests

Script per il testing manuale degli endpoint REST via `curl` + `jq`.
Non fa parte della suite automatica — va eseguito a mano contro un'istanza in esecuzione.

## Prerequisiti

- `curl` e `jq` installati
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

## Flusso tipico

```bash
# 1. Registra un cliente e uno staff
./test-manual.sh auth:register
./test-manual.sh auth:register-staff   # richiede STAFF_TOKEN già impostato

# 2. Login e recupero token
./test-manual.sh auth:login            # → copia il token dall'output
export TOKEN="eyJ..."

./test-manual.sh auth:login-staff      # → copia il token dall'output
export STAFF_TOKEN="eyJ..."

# 3. Ora puoi eseguire gli endpoint protetti
./test-manual.sh immobile:crea
export IMMOBILE_ID=<id restituito>

./test-manual.sh offerta:aggiungi
./test-manual.sh visita:prenota
```

## Comandi

### `auth`

| Comando | Endpoint | HTTP atteso | Note |
|---|---|---|---|
| `auth:register` | `POST /auth/register` | 201 | Registra un cliente di test |
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
| `visita:prenota` | `POST /visita/prenota` | 201 | Prenota una visita su `IMMOBILE_ID` — richiede `TOKEN` |
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
