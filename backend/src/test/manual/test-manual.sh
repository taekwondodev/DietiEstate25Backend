#!/usr/bin/env bash
# DietiEstates25 — Manual API test script
#
# Usage:
#   ./test-manual.sh <section>[:<test>]
#
# Examples:
#   ./test-manual.sh auth
#   ./test-manual.sh auth:login
#   ./test-manual.sh immobile
#   ./test-manual.sh immobile:crea
#   ./test-manual.sh offerta
#   ./test-manual.sh visita:prenota
#   ./test-manual.sh geodata
#   ./test-manual.sh meteo
#   ./test-manual.sh all
#
# After login, export tokens before running protected endpoints:
#   export TOKEN="eyJ..."
#   export STAFF_TOKEN="eyJ..."

BASE_URL="${BASE_URL:-http://localhost:8080}"
TOKEN="${TOKEN:-}"
STAFF_TOKEN="${STAFF_TOKEN:-}"
IMMOBILE_ID="${IMMOBILE_ID:-1}"
OFFERTA_ID="${OFFERTA_ID:-1}"
VISITA_ID="${VISITA_ID:-1}"

# ─── helpers ───────────────────────────────────────────────────────────────

sep()  { echo; echo "── $1 ──────────────────────────────"; }
ok()   { curl -s -w "\n[HTTP %{http_code}]\n" "$@" | jq . 2>/dev/null || cat; }
auth() { echo "Authorization: Bearer $1"; }

require_token() {
  local var=$1 name=$2
  if [[ -z "${!var}" ]]; then
    echo "ERROR: $name not set. Run auth:login or auth:login-staff first, then:"
    echo "  export $var=\"<token>\""
    exit 1
  fi
}

# ─── AUTH ──────────────────────────────────────────────────────────────────

auth:register() {
  sep "AUTH: Register cliente"
  ok -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d '{"email":"cliente@test.com","password":"Password123!","role":"Cliente"}'
}

auth:register-staff() {
  require_token STAFF_TOKEN STAFF_TOKEN
  sep "AUTH: Register staff (Admin/Gestore role required)"
  ok -X POST "$BASE_URL/auth/register-staff" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $STAFF_TOKEN" \
    -d '{"email":"agente@test.com","password":"Password123!","role":"AgenteImmobiliare"}'
}

auth:login() {
  sep "AUTH: Login cliente"
  ok -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"cliente@test.com","password":"Password123!"}'
  echo "→ copy token and run: export TOKEN=\"<token>\""
}

auth:login-staff() {
  sep "AUTH: Login staff"
  ok -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"agente@test.com","password":"Password123!"}'
  echo "→ copy token and run: export STAFF_TOKEN=\"<token>\""
}

auth:login-bad() {
  sep "AUTH: Login bad password → expect 404"
  ok -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"cliente@test.com","password":"wrong"}'
}

auth() {
  auth:register
  auth:login
  auth:login-staff
  auth:login-bad
}

# ─── IMMOBILI ──────────────────────────────────────────────────────────────

immobile:cerca() {
  sep "IMMOBILE: Cerca (public)"
  ok -G "$BASE_URL/immobile/cerca" \
    --data-urlencode "comune=Napoli" \
    --data-urlencode "page=0" \
    --data-urlencode "size=5"
}

immobile:cerca-filtri() {
  sep "IMMOBILE: Cerca con filtri"
  ok -G "$BASE_URL/immobile/cerca" \
    --data-urlencode "comune=Napoli" \
    --data-urlencode "prezzoMin=50000" \
    --data-urlencode "prezzoMax=300000" \
    --data-urlencode "nBagni=2" \
    --data-urlencode "tipologia=Appartamento" \
    --data-urlencode "page=0" \
    --data-urlencode "size=10"
}

immobile:cerca-bad() {
  sep "IMMOBILE: Cerca prezzoMin > prezzoMax → expect 400"
  ok -G "$BASE_URL/immobile/cerca" \
    --data-urlencode "comune=Napoli" \
    --data-urlencode "prezzoMin=500000" \
    --data-urlencode "prezzoMax=100"
}

immobile:crea() {
  require_token STAFF_TOKEN STAFF_TOKEN
  sep "IMMOBILE: Crea"
  ok -X POST "$BASE_URL/immobile/crea" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $STAFF_TOKEN" \
    -d '{
      "descrizione": "Bellissimo appartamento luminoso con vista mare, completamente ristrutturato.",
      "urlFoto": "https://example.com/foto.jpg",
      "prezzo": 250000.00,
      "dimensione": 85.5,
      "nBagni": 2,
      "nStanze": 4,
      "tipologia": "Appartamento",
      "indirizzo": "Via Roma 42, Napoli",
      "comune": "Napoli",
      "piano": 3,
      "hasAscensore": true,
      "hasBalcone": true
    }'
}

immobile:personali() {
  require_token STAFF_TOKEN STAFF_TOKEN
  sep "IMMOBILE: Personali"
  ok "$BASE_URL/immobile/personali" \
    -H "Authorization: Bearer $STAFF_TOKEN"
}

immobile() {
  immobile:cerca
  immobile:cerca-filtri
  immobile:cerca-bad
  immobile:crea
  immobile:personali
}

# ─── OFFERTE ───────────────────────────────────────────────────────────────

offerta:aggiungi() {
  require_token TOKEN TOKEN
  sep "OFFERTA: Aggiungi (idImmobile=$IMMOBILE_ID)"
  ok -X POST "$BASE_URL/offerta/aggiungi" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"importo\":230000.00,\"idImmobile\":$IMMOBILE_ID}"
}

offerta:accetta() {
  require_token STAFF_TOKEN STAFF_TOKEN
  sep "OFFERTA: Aggiorna → Accettata (idOfferta=$OFFERTA_ID)"
  ok -X PATCH "$BASE_URL/offerta/aggiorna" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $STAFF_TOKEN" \
    -d "{\"idOfferta\":$OFFERTA_ID,\"stato\":\"Accettata\"}"
}

offerta:rifiuta() {
  require_token STAFF_TOKEN STAFF_TOKEN
  sep "OFFERTA: Aggiorna → Rifiutata (idOfferta=$OFFERTA_ID)"
  ok -X PATCH "$BASE_URL/offerta/aggiorna" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $STAFF_TOKEN" \
    -d "{\"idOfferta\":$OFFERTA_ID,\"stato\":\"Rifiutata\"}"
}

offerta:riepilogo-cliente() {
  require_token TOKEN TOKEN
  sep "OFFERTA: Riepilogo cliente"
  ok "$BASE_URL/offerta/riepilogoCliente" \
    -H "Authorization: Bearer $TOKEN"
}

offerta:riepilogo-agenzia() {
  require_token STAFF_TOKEN STAFF_TOKEN
  sep "OFFERTA: Riepilogo utente agenzia"
  ok "$BASE_URL/offerta/riepilogoUtenteAgenzia" \
    -H "Authorization: Bearer $STAFF_TOKEN"
}

offerta() {
  offerta:aggiungi
  offerta:riepilogo-cliente
  offerta:riepilogo-agenzia
}

# ─── VISITE ────────────────────────────────────────────────────────────────

visita:prenota() {
  require_token TOKEN TOKEN
  sep "VISITA: Prenota (idImmobile=$IMMOBILE_ID)"
  ok -X POST "$BASE_URL/visita/prenota" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
      \"idImmobile\": $IMMOBILE_ID,
      \"dataVisita\": \"2026-05-01\",
      \"oraVisita\": \"10:00:00\"
    }"
}

visita:prenota-bad() {
  require_token TOKEN TOKEN
  sep "VISITA: Prenota ora fuori range → expect 400"
  ok -X POST "$BASE_URL/visita/prenota" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
      \"idImmobile\": $IMMOBILE_ID,
      \"dataVisita\": \"2026-05-01\",
      \"oraVisita\": \"22:00:00\"
    }"
}

visita:conferma() {
  require_token STAFF_TOKEN STAFF_TOKEN
  sep "VISITA: Aggiorna → Confermata (idVisita=$VISITA_ID)"
  ok -X PATCH "$BASE_URL/visita/aggiorna" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $STAFF_TOKEN" \
    -d "{\"idVisita\":$VISITA_ID,\"stato\":\"Confermata\"}"
}

visita:rifiuta() {
  require_token STAFF_TOKEN STAFF_TOKEN
  sep "VISITA: Aggiorna → Rifiutata (idVisita=$VISITA_ID)"
  ok -X PATCH "$BASE_URL/visita/aggiorna" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $STAFF_TOKEN" \
    -d "{\"idVisita\":$VISITA_ID,\"stato\":\"Rifiutata\"}"
}

visita:riepilogo-cliente() {
  require_token TOKEN TOKEN
  sep "VISITA: Riepilogo cliente"
  ok "$BASE_URL/visita/riepilogoCliente" \
    -H "Authorization: Bearer $TOKEN"
}

visita:riepilogo-agenzia() {
  require_token STAFF_TOKEN STAFF_TOKEN
  sep "VISITA: Riepilogo utente agenzia"
  ok "$BASE_URL/visita/riepilogoUtenteAgenzia" \
    -H "Authorization: Bearer $STAFF_TOKEN"
}

visita() {
  visita:prenota
  visita:prenota-bad
  visita:riepilogo-cliente
  visita:riepilogo-agenzia
}

# ─── SERVIZI ESTERNI ───────────────────────────────────────────────────────

geodata() {
  sep "GEODATA: Punti di interesse"
  ok -X POST "$BASE_URL/geodata" \
    -H "Content-Type: application/json" \
    -d '{
      "latitudine": 40.8522,
      "longitudine": 14.2681,
      "raggio": 1000,
      "categorie": ["parco", "trasporto", "scuola"]
    }'

  sep "GEODATA: Raggio 0 → expect 400"
  ok -X POST "$BASE_URL/geodata" \
    -H "Content-Type: application/json" \
    -d '{
      "latitudine": 40.8522,
      "longitudine": 14.2681,
      "raggio": 0,
      "categorie": ["parco"]
    }'
}

meteo() {
  sep "METEO: Previsioni"
  ok -X POST "$BASE_URL/meteo" \
    -H "Content-Type: application/json" \
    -d '{
      "latitudine": "40.8522",
      "longitudine": "14.2681",
      "date": "2026-04-14"
    }'

  sep "METEO: Data nel passato → expect 400"
  ok -X POST "$BASE_URL/meteo" \
    -H "Content-Type: application/json" \
    -d '{
      "latitudine": "40.8522",
      "longitudine": "14.2681",
      "date": "2020-01-01"
    }'
}

# ─── ALL ───────────────────────────────────────────────────────────────────

all() {
  auth
  immobile
  offerta
  visita
  geodata
  meteo
}

# ─── help ──────────────────────────────────────────────────────────────────

usage() {
  echo "Usage: $0 <section>[:<test>]"
  echo
  echo "Sections:"
  echo "  all"
  echo "  auth                     auth:register  auth:register-staff"
  echo "                           auth:login     auth:login-staff  auth:login-bad"
  echo "  immobile                 immobile:cerca  immobile:cerca-filtri  immobile:cerca-bad"
  echo "                           immobile:crea   immobile:personali"
  echo "  offerta                  offerta:aggiungi  offerta:accetta  offerta:rifiuta"
  echo "                           offerta:riepilogo-cliente  offerta:riepilogo-agenzia"
  echo "  visita                   visita:prenota  visita:prenota-bad  visita:conferma"
  echo "                           visita:rifiuta  visita:riepilogo-cliente  visita:riepilogo-agenzia"
  echo "  geodata"
  echo "  meteo"
  echo
  echo "Env vars:"
  echo "  BASE_URL      (default: http://localhost:8080)"
  echo "  TOKEN         JWT for cliente"
  echo "  STAFF_TOKEN   JWT for agente/gestore/admin"
  echo "  IMMOBILE_ID   (default: 1)"
  echo "  OFFERTA_ID    (default: 1)"
  echo "  VISITA_ID     (default: 1)"
}

# ─── dispatch ──────────────────────────────────────────────────────────────

CMD="${1:-}"
if [[ -z "$CMD" ]]; then
  usage
  exit 0
fi

if declare -f "$CMD" > /dev/null 2>&1; then
  "$CMD"
else
  echo "ERROR: unknown command '$CMD'"
  echo
  usage
  exit 1
fi
