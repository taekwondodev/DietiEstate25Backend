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
  backend/
    secret.yaml                           ← Tutte le variabili d'ambiente (da compilare)
    deployment.yaml                       ← Pod backend, attende postgres prima di avviarsi
    service.yaml                          ← NodePort :30080 (accessibile dall'esterno)
  test/
    postgres-test-deployment.yaml         ← Postgres isolato per i test (user/pass/db fissi)
    postgres-test-service.yaml            ← Service "postgres-test" (nome richiesto da application-test.properties)
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

## Build e caricamento immagine

Kind usa il proprio daemon container isolato da Podman.
L'immagine va esportata da Podman e caricata manualmente nel cluster.
Il Deployment usa `imagePullPolicy: Never` — non tenta pull da registry esterni.

```bash
podman build -t dietiestate25-backend:latest ./backend
podman save dietiestate25-backend:latest -o /tmp/backend.tar
kind load image-archive /tmp/backend.tar --name dietiestate25
```

> Ripeti questo step ogni volta che modifichi il codice.

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

Verifica che i Pod siano Running:

```bash
kubectl get pods -n dietiestate25
```

---

## Accesso all'API

Il backend è esposto su NodePort `30080`. Recupera l'IP del nodo:

```bash
kubectl get nodes -o wide
```

L'API è raggiungibile a `http://<NODE_IP>:30080`.

---

## Esecuzione test di integrazione

I test richiedono un Postgres dedicato (`postgres-test`) nella stessa namespace.
Il datasource è hardcoded in `application-test.properties` a `postgres-test:5432/test_db`
con credenziali `test`/`test` — il Service deve chiamarsi esattamente `postgres-test`.

### 1. Build e caricamento immagine test

```bash
podman build -t dietiestate25-backend-test:latest -f ./backend/Dockerfile.test ./backend
podman save dietiestate25-backend-test:latest -o /tmp/backend-test.tar
kind load image-archive /tmp/backend-test.tar --name dietiestate25
```

### 2. Avvia Postgres test e lancia il Job

```bash
kubectl apply -f k8s/test/
```

Il Job attende che `postgres-test` sia pronto (initContainer), poi esegue `mvn clean test`
e termina. `backoffLimit: 0` fa sì che non venga ritentato in caso di fallimento.

### 3. Segui i log in tempo reale

```bash
kubectl logs -n dietiestate25 job/backend-test -f
```

### 4. Controlla l'esito

```bash
kubectl get job backend-test -n dietiestate25
# COMPLETIONS 1/1 → tutti i test sono passati
# COMPLETIONS 0/1 → fallimento, leggi i log sopra
```

### 5. Pulizia dopo i test

```bash
# Elimina Job e Postgres test (il cluster e i dati di produzione rimangono intatti)
kubectl delete -f k8s/test/
```

> Per rieseguire i test è necessario eliminare il Job prima di riapplicarlo,
> perché i Job completati non vengono sovrascritti da `kubectl apply`.
> Usa `kubectl delete job backend-test -n dietiestate25` e poi `kubectl apply -f k8s/test/`.

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

```bash
# 1. Avvia Podman Machine e crea il cluster (solo la prima volta)
podman machine start
KIND_EXPERIMENTAL_PROVIDER=podman kind create cluster --name dietiestate25

# 2. Build e carica immagine nel cluster
podman build -t dietiestate25-backend:latest ./backend
podman save dietiestate25-backend:latest -o /tmp/backend.tar
kind load image-archive /tmp/backend.tar --name dietiestate25

# 3. Applica i manifesti
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/backend/

# 4. Verifica che i Pod siano Running
kubectl get pods -n dietiestate25

# 5. Recupera l'IP del nodo e accedi all'API
kubectl get nodes -o wide
# http://<NODE_IP>:30080
```

---

## Recap — Esecuzione test

```bash
# 1. Assicurati che il cluster sia attivo e il namespace esista
podman machine start
KIND_EXPERIMENTAL_PROVIDER=podman kind create cluster --name dietiestate25  # se non esiste già
kubectl apply -f k8s/namespace.yaml

# 2. Build e carica immagine test nel cluster
podman build -t dietiestate25-backend-test:latest -f ./backend/Dockerfile.test ./backend
podman save dietiestate25-backend-test:latest -o /tmp/backend-test.tar
kind load image-archive /tmp/backend-test.tar --name dietiestate25

# 3. Applica anche il ConfigMap (usato da postgres-test)
kubectl apply -f k8s/postgres/configmap-init.yaml

# 4. Avvia Postgres test e lancia il Job
kubectl apply -f k8s/test/

# 5. Segui i log in tempo reale
kubectl logs -n dietiestate25 job/backend-test -f

# 6. Controlla l'esito
kubectl get job backend-test -n dietiestate25
# COMPLETIONS 1/1 → tutti i test sono passati
# COMPLETIONS 0/1 → fallimento, leggi i log

# 7. Pulizia dopo i test
kubectl delete -f k8s/test/

# Per rieseguire i test:
kubectl delete job backend-test -n dietiestate25
kubectl apply -f k8s/test/
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
