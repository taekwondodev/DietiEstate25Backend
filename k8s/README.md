# Kubernetes — Local Setup

## Dipendenze

### Kind
Kind (Kubernetes in Docker) esegue un cluster Kubernetes completo dentro container Podman.
È l'alternativa stabile a Minikube quando si usa Podman su macOS: il driver Podman di Minikube
è ancora segnato come sperimentale e presenta bug noti (CoreDNS failures, incompatibilità CRI-O).
Kind dalla v0.29+ ha supporto Podman stabile e maturo.

```bash
brew install kind
```

### kubectl
kubectl è la CLI ufficiale per interagire con il cluster Kubernetes (apply, get, logs, ecc.).
Kind crea il cluster ma non include kubectl — va installato separatamente.

```bash
brew install kubectl
```

---

## Struttura manifesti

```
k8s/
  namespace.yaml                          ← Namespace "dietiestate25"
  postgres/
    configmap-init.yaml                   ← Script SQL montati su /docker-entrypoint-initdb.d/
    secret.yaml                           ← Credenziali postgres (da compilare)
    pvc.yaml                              ← Volume persistente 1Gi per i dati
    deployment.yaml                       ← Pod postgres con readinessProbe
    service.yaml                          ← ClusterIP (visibile solo internamente al cluster)
    network-policy.yaml                   ← Ingress deny-all; permette solo traffico da backend:5432
  backend/
    secret.yaml                           ← Tutte le variabili d'ambiente (da compilare)
    deployment.yaml                       ← Pod backend, attende postgres prima di avviarsi
    service.yaml                          ← NodePort :30080 (accessibile dall'esterno)
  test/
    namespace-test.yaml                   ← Namespace "dietiestate25-test" (isolato da produzione)
    configmap-init-test.yaml              ← Stessi script SQL del prod, namespace dietiestate25-test
    postgres-test-deployment.yaml         ← Postgres isolato per i test (user/pass/db fissi)
    postgres-test-service.yaml            ← Service "postgres-test" (nome richiesto da application-test.properties)
    network-policy-postgres-test.yaml     ← Ingress deny-all; permette solo traffico da backend-test:5432
    backend-test-job.yaml                 ← Job che esegue mvn clean test e termina
```

---

## Setup iniziale

### 1. Avvia Podman Machine

```bash
podman machine start
```

### 2. Crea il cluster Kind

```bash
KIND_EXPERIMENTAL_PROVIDER=podman kind create cluster --name dietiestate25
```

### 3. Verifica che kubectl punti al cluster

```bash
kubectl cluster-info --context kind-dietiestate25
```

---

## Immagine di produzione

L'immagine di produzione viene pubblicata automaticamente su Docker Hub dalla pipeline CI/CD
(`taekwondodev/dietiestate25-backend:latest`) al termine di tutti i check di qualità e sicurezza.
Il Deployment usa `imagePullPolicy: Always` — kind la scarica direttamente dal registry ad ogni avvio.
Non è necessaria nessuna operazione manuale di build o caricamento.

---

## Compilare i Secret

I valori nei file `secret.yaml` vanno codificati in base64.

```bash
echo -n "il-tuo-valore" | base64
```

Esempi per `postgres/secret.yaml`:

| Chiave | Valore esempio |
|---|---|
| `POSTGRES_USER` | `dietiestate25` |
| `POSTGRES_PASSWORD` | `password-sicura` |
| `POSTGRES_DB` | `dietiestate25` |

Per `backend/secret.yaml`, il datasource URL punta al Service interno:

```
jdbc:postgresql://postgres:5432/<POSTGRES_DB>
```

> I file `secret.yaml` **non devono essere committati** con valori reali.
> Aggiungili a `.gitignore` se usi credenziali non di test.

---

## Deploy

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/backend/
```

> `k8s/postgres/` include la `NetworkPolicy` che nega tutto l'ingress su postgres
> tranne il traffico proveniente dal pod `backend` sulla porta 5432.
> Richiede kind >= v0.24.0.

Verifica che i Pod siano Running:

```bash
kubectl get pods -n dietiestate25
```

---

## Accesso all'API

Con kind su Podman su macOS, l'IP del nodo non è raggiungibile direttamente dall'host.
Usa il port-forward:

```bash
kubectl port-forward -n dietiestate25 deployment/backend 8080:8080
```

L'API è raggiungibile a `http://localhost:8080`.

> Il NodePort `30080` è definito nel Service ma non è accessibile direttamente su macOS con Podman.

---

## Esecuzione test di integrazione

I test girano nel namespace `dietiestate25-test`, isolato dalla produzione.
Il datasource è hardcoded in `application-test.properties` a `postgres-test:5432/test_db`
con credenziali `test`/`test` — il Service deve chiamarsi esattamente `postgres-test`.

### 1. Build e caricamento immagine test

```bash
podman build -t localhost/dietiestate25-backend-test:latest -f ./backend/Dockerfile.test ./backend
podman save localhost/dietiestate25-backend-test:latest -o /tmp/backend-test.tar
KIND_EXPERIMENTAL_PROVIDER=podman kind load image-archive /tmp/backend-test.tar --name dietiestate25
```

### 2. Avvia Postgres test e lancia il Job

```bash
kubectl apply -f k8s/test/namespace-test.yaml
kubectl apply -f k8s/test/
```

Il Job attende che `postgres-test` sia pronto (initContainer), poi esegue `mvn clean test`
e termina. `backoffLimit: 0` fa sì che non venga ritentato in caso di fallimento.

### 3. Segui i log in tempo reale

```bash
kubectl logs -n dietiestate25-test job/backend-test -f
```

### 4. Controlla l'esito

```bash
kubectl get job backend-test -n dietiestate25-test
# COMPLETIONS 1/1 → tutti i test sono passati
# COMPLETIONS 0/1 → fallimento, leggi i log sopra
```

### 5. Pulizia dopo i test

```bash
# Elimina solo il Job (postgres-test rimane attivo per riesecuzioni rapide)
kubectl delete job backend-test -n dietiestate25-test

# Pulizia completa (namespace incluso)
kubectl delete namespace dietiestate25-test
```

> Per rieseguire i test dopo la pulizia del solo Job:
> ```bash
> kubectl apply -f k8s/test/backend-test-job.yaml
> ```
> Per rieseguire dopo pulizia completa del namespace, usa i comandi del punto 2.

---

## Comandi utili

```bash
# Log del backend
kubectl logs -n dietiestate25 deployment/backend -f

# Log di postgres
kubectl logs -n dietiestate25 deployment/postgres -f

# Stato dei pod
kubectl get pods -n dietiestate25

# Distruggi il cluster
kind delete cluster --name dietiestate25
```

---

## Recap — Avvio produzione

L'immagine di produzione viene scaricata automaticamente da Docker Hub (`imagePullPolicy: Always`).
Non è necessaria nessuna build locale.

```bash
# 1. Avvia Podman Machine e crea il cluster (solo la prima volta)
podman machine start
KIND_EXPERIMENTAL_PROVIDER=podman kind create cluster --name dietiestate25

# 2. Applica i manifesti (l'immagine viene scaricata da Docker Hub)
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/backend/

# 3. Verifica che i Pod siano Running
kubectl get pods -n dietiestate25

# 4. Accedi all'API tramite port-forward
kubectl port-forward -n dietiestate25 deployment/backend 8080:8080
# http://localhost:8080
```

Per aggiornare all'ultima immagine pubblicata dalla pipeline:

```bash
kubectl rollout restart deployment/backend -n dietiestate25
```

---

## Recap — Esecuzione test

```bash
# 1. Assicurati che il cluster sia attivo
podman machine start
KIND_EXPERIMENTAL_PROVIDER=podman kind create cluster --name dietiestate25  # se non esiste già

# 2. Build e carica immagine test nel cluster
podman build -t localhost/dietiestate25-backend-test:latest -f ./backend/Dockerfile.test ./backend
podman save localhost/dietiestate25-backend-test:latest -o /tmp/backend-test.tar
KIND_EXPERIMENTAL_PROVIDER=podman kind load image-archive /tmp/backend-test.tar --name dietiestate25

# 3. Crea il namespace di test e applica tutti i manifest
kubectl apply -f k8s/test/namespace-test.yaml
kubectl apply -f k8s/test/

# 4. Segui i log in tempo reale
kubectl logs -n dietiestate25-test job/backend-test -f

# 5. Controlla l'esito
kubectl get job backend-test -n dietiestate25-test
# COMPLETIONS 1/1 → tutti i test sono passati
# COMPLETIONS 0/1 → fallimento, leggi i log

# 6. Pulizia dopo i test
# Solo Job (postgres-test rimane attivo):
kubectl delete job backend-test -n dietiestate25-test
# Pulizia completa:
kubectl delete namespace dietiestate25-test

# Per rieseguire (dopo pulizia solo Job):
kubectl apply -f k8s/test/backend-test-job.yaml
```

---

## Fermare il cluster

**Sospendi** (il cluster Kind viene preservato, i Pod ripartono al prossimo avvio):

```bash
podman machine stop
# per riprendere:
podman machine start
```

**Distruggi tutto** (libera spazio su disco e risorse):

```bash
kind delete cluster --name dietiestate25
podman machine stop
```
