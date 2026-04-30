# Kubernetes — Scelte e Motivazioni

I comandi di setup ed esecuzione locale usati → [k8s/README.md](../../k8s/README.md)

---

## Kind

Kind (Kubernetes in Docker) esegue un cluster Kubernetes completo dentro container. È stato scelto perché è l'alternativa stabile a Minikube quando si usa **Podman su macOS**: il driver Podman di Minikube è instabile e non supportato ufficialmente, mentre Kind funziona in modo affidabile con `KIND_EXPERIMENTAL_PROVIDER=podman`.

Permette di replicare localmente lo stesso ambiente Kubernetes usato in produzione, incluse NetworkPolicy e namespace isolation, senza dipendere da un cluster cloud.

---

## Kubectl

kubectl è la CLI ufficiale per interagire con il cluster Kubernetes. Kind crea il cluster ma non include un client — kubectl va installato separatamente. È lo standard de facto: tutti i manifest e i comandi del progetto usano kubectl, ed è quello che la documentazione ufficiale di Kubernetes riferisce.

---

## NetworkPolicy — Isolamento di PostgreSQL

[`k8s/postgres/network-policy.yaml`](../../k8s/postgres/network-policy.yaml) nega tutto l'ingress su PostgreSQL per default e ammette solo il traffico proveniente dal pod `backend` sulla porta 5432. La stessa policy è applicata al namespace di test tramite [`k8s/test/network-policy-postgres-test.yaml`](../../k8s/test/network-policy-postgres-test.yaml).

Scelta basata sul principio del minimo privilegio applicato a livello di rete: ogni comunicazione non esplicitamente autorizzata è negata. Senza questa policy, qualsiasi pod nel cluster potrebbe raggiungere PostgreSQL direttamente, bypassando il backend e la sua logica di autorizzazione.

---

## ClusterIP per PostgreSQL

[`k8s/postgres/service.yaml`](../../k8s/postgres/service.yaml) definisce esplicitamente `type: ClusterIP`. PostgreSQL non è raggiungibile dall'esterno del cluster — solo i pod interni al namespace possono risolverlo tramite DNS (`postgres:5432`). L'unico punto di accesso esterno è il backend, esposto come `NodePort` in [`k8s/backend/service.yaml`](../../k8s/backend/service.yaml).

---

## Gestione dei Secret

I file `secret.yaml` non sono version controllati. Variabili, generazione e meccanismo di iniezione → [docs/secrets/README.md](../secrets/README.md).

---

## Isolamento Namespace — Produzione vs Test

I test girano nel namespace `dietiestate25-test` ([`k8s/test/namespace-test.yaml`](../../k8s/test/namespace-test.yaml)), completamente isolato dal namespace `dietiestate25` di produzione. Il namespace di test ha il proprio pod PostgreSQL con credenziali fisse (`test`/`test`) e la stessa NetworkPolicy di isolamento replicata. Nessuna risorsa è condivisa tra i due namespace.

La separazione evita tre problemi: i test scrivono e cancellano dati liberamente senza toccare dati reali; le credenziali di produzione non vengono mai esposte ai test; il namespace di test può essere distrutto interamente senza rischi.

---

## ReadinessProbe su PostgreSQL

[`k8s/postgres/deployment.yaml`](../../k8s/postgres/deployment.yaml) definisce una `readinessProbe` che esegue `pg_isready` ogni 5 secondi. Kubernetes segna il pod PostgreSQL come `Ready` solo quando il database accetta connessioni effettive. Il backend aspetta che la probe sia soddisfatta prima di avviarsi, evitando `CrashLoopBackOff` da connessione fallita durante lo startup.

---

## Separazione della porta di management (8081)

`management.server.port=8081` in [`application.properties`](../../backend/src/main/resources/application.properties) fa partire il management server di Spring Boot su una porta separata rispetto alla porta applicativa 8080. La readiness probe in [`k8s/backend/deployment.yaml`](../../k8s/backend/deployment.yaml) punta a 8081 — Kubernetes raggiunge questa porta direttamente sul pod tramite la rete interna del cluster.

La porta 8081 è dichiarata come `containerPort` nel deployment (informativa per il cluster) ma è intenzionalmente assente dal [`k8s/backend/service.yaml`](../../k8s/backend/service.yaml): il `Service` di tipo `NodePort` espone solo la porta 8080 verso l'esterno. Di conseguenza `/actuator/health` non è raggiungibile da fuori il cluster — né via NodePort né via nessun altro path esposto. Questa separazione è la mitigazione diretta del finding Medium `40042` rilevato da OWASP ZAP, che segnalava l'endpoint di health accessibile sulla stessa superficie esterna delle API applicative.
