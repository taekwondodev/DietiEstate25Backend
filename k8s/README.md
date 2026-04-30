# Kubernetes — Local Setup

## Dipendenze

```bash
brew install kind

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

## Compilare i Secret

I valori nei file `secret.yaml` vanno codificati in base64.

```bash
echo -n "il-tuo-valore" | base64
```

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

## Mappare l'API su porta fisica

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

### 3. Controlla l'esito

```bash
kubectl get job backend-test -n dietiestate25-test
# COMPLETIONS 1/1 → tutti i test sono passati
# COMPLETIONS 0/1 → fallimento, leggi i log sopra
```

### 4. Pulizia dopo i test

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
# Distruggi il cluster
kind delete cluster --name dietiestate25
# Fermi podman machine
podman machine stop

# Per aggiornare all'ultima immagine pubblicata dalla pipeline
kubectl rollout restart deployment/backend -n dietiestate25
```
