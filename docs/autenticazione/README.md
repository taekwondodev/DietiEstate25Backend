# Autenticazione e Sicurezza

## Migrazione da AWS Cognito a Spring Security

**Prima:** l'autenticazione era delegata interamente ad AWS Cognito. Il backend non gestiva né le password né i token — Cognito emetteva i JWT, li validava e controllava il ciclo di vita degli utenti. Il backend si limitava a fidarsi dei token ricevuti.

**Problema:** dipendenza totale da un servizio esterno a pagamento, nessun controllo sulla logica di autenticazione, impossibilità di testare la security layer in isolamento.

**Ora:** registrazione, login, hashing delle password, emissione e validazione JWT sono gestiti internamente da Spring Security + PostgreSQL.

---

## JWT — Emissione del Token

**Prima:** Cognito emetteva il token dopo il login. Il backend non aveva visibilità sul contenuto del token né sulla chiave di firma.

**Ora:** il token è emesso da [`JwtService#generateToken`](../../backend/src/main/java/com/dietiestate25backend/service/JwtService.java#L30) e richiamato da [`AuthService`](../../backend/src/main/java/com/dietiestate25backend/service/AuthService.java#L68) al termine del login.

**Perché JWT:**
- **Stateless** — il server non memorizza sessioni; ogni richiesta è autosufficiente
- **Compatto** — trasmissibile nell'header HTTP `Authorization: Bearer <token>`
- **Verificabile** — la firma impedisce il tampering del payload
- **Interoperabile** — standard RFC 7519, supportato da ogni linguaggio

**Flusso operativo:**
1. Client invia `email` + `password`
2. Server valida le credenziali e genera un JWT con: `uid`, `role`, `email`
3. Server restituisce il token al client
4. Client allega il token in ogni richiesta successiva (`Authorization: Bearer <token>`)
5. Server verifica la firma senza interrogare il database

---

## Struttura del Payload

Il payload contiene i **claim**: dichiarazioni sull'utente e metadati del token.

```json
{
  "sub": "uid-agente-001",
  "iat": 1711420800,
  "exp": 1711424400,
  "role": "AgenteImmobiliare",
  "email": "agente1@test.it"
}
```

| Claim | Significato | Tipo |
|-------|-------------|------|
| `sub` | Subject — identificativo univoco (uid) dell'utente | String |
| `iat` | Issued At — timestamp di emissione | Long |
| `exp` | Expiration — timestamp di scadenza | Long |
| `role` | Custom claim — ruolo dell'utente nel sistema RBAC | String |
| `email` | Custom claim — email dell'utente | String |

Poiché il JWT è auto-contenuto e firmato, il server estrae i claim direttamente dal payload senza interrogare il database — la firma HMAC-SHA256 garantisce che non siano stati alterati.

---

## Firma HMAC-SHA256 e Secret

**Prima:** Cognito gestiva chiavi di firma internamente (RS256 con chiavi asimmetriche pubblicate via JWKS). Il backend non aveva accesso né controllo sulla chiave.

**Ora:** il server usa HMAC-SHA256 con un secret simmetrico a 256 bit, configurato in [`SecurityConfig#jwtEncoder`](../../backend/src/main/java/com/dietiestate25backend/utils/SecurityConfig.java#L52) e [`SecurityConfig#jwtDecoder`](../../backend/src/main/java/com/dietiestate25backend/utils/SecurityConfig.java#L59).

**Garantisce:**
1. **Integrità** — qualsiasi modifica al payload invalida la firma
2. **Autenticità** — solo chi conosce il secret può produrre un token valido
3. **Non ripudio** — il server può provare di aver emesso il token

**Generazione del secret:**

```bash
openssl rand -base64 32
```

Il risultato (32 byte = 256 bit) viene iniettato come variabile d'ambiente `app.jwt.secret` tramite un Kubernetes Secret definito in `k8s/backend/secret.yaml`, non version controllato. Mai hardcoded nel codice.

---

## TTL e Assenza di Refresh Token

**TTL:** 1 ora, definito in [`JwtService`](../../backend/src/main/java/com/dietiestate25backend/service/JwtService.java#L17). Alla scadenza il token non è più accettato dal server e l'utente deve ri-autenticarsi.

**Scelta progettuale — nessun refresh token.** Intenzionale, pensato per app mobile.

| | |
|---|---|
| **Pro** | Meno superfici di attacco (un token in meno da proteggere), nessuna blacklist da gestire, minor complessità lato client e server |
| **Contro** | Token rubato utilizzabile fino alla scadenza (non revocabile), ri-autenticazione richiesta ogni ora, modello non adatto ad applicazioni web |

---

## SecurityFilterChain

**Prima:** nessun filter chain nel backend. Cognito intercettava le richieste a monte tramite un API Gateway configurato esternamente. Il backend riceveva solo richieste già autorizzate.

**Ora:** [`SecurityConfig#securityFilterChain`](../../backend/src/main/java/com/dietiestate25backend/utils/SecurityConfig.java#L28) intercetta ogni richiesta HTTP, valida il JWT, estrae il `SecurityContext` e blocca le richieste non autorizzate prima che raggiungano il controller.

**Flusso per ogni richiesta:**
1. Verifica se l'endpoint è pubblico → se sì, propaga senza token
2. Estrae il JWT dall'header `Authorization`
3. Valida la firma HMAC-SHA256
4. Popola il `SecurityContext` con i claim del token
5. Autorizza o restituisce 401/403

**Scelte di configurazione:**

| Scelta | Valore | Prima | Dopo |
|--------|--------|-------|------|
| **CSRF** | `disable` | Non gestito (Cognito API Gateway) | Disabilitato perché le API REST mobile non usano cookie — CSRF non è applicabile |
| **Session Policy** | `STATELESS` | Cognito gestiva le sessioni | Nessuna sessione HTTP; ogni richiesta porta il proprio token |
| **Endpoint pubblici** | `permitAll()` su `/auth/login`, `/auth/register`, `/immobile/cerca`, `/actuator/health` | Definiti nel Gateway Cognito | Definiti esplicitamente nel filter chain; tutto il resto richiede autenticazione |
| **OAuth2 Resource Server** | JWT decoder abilitato | Cognito validava i token | Spring Security decodifica e verifica la firma automaticamente ad ogni richiesta |

---

## BCrypt — Hashing della Password

**Prima:** Cognito memorizzava e gestiva le password internamente. Il backend non le toccava mai.

**Ora:** le password sono hashate con BCrypt al momento della registrazione tramite [`SecurityConfig#passwordEncoder`](../../backend/src/main/java/com/dietiestate25backend/utils/SecurityConfig.java#L47), richiamato in [`AuthService`](../../backend/src/main/java/com/dietiestate25backend/service/AuthService.java#L81).

**Perché BCrypt e non SHA-256 o MD5:**

BCrypt è una **key derivation function** progettata specificamente per le password — non è un hash generico.

| Proprietà | Effetto |
|-----------|---------|
| **Salt obbligatorio** | Protegge da rainbow table attack; due password identiche producono hash diversi |
| **Cost factor (iterazioni)** | Rende il brute-force computazionalmente costoso anche con hardware dedicato |
| **Anti-timing attack** | Il tempo di verifica è uniforme indipendentemente dalla correttezza della password |
| **Standard OWASP** | Raccomandato da OWASP Password Storage Cheat Sheet come algoritmo di default |
