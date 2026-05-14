#!/usr/bin/env bash
set -euo pipefail

CLUSTER="dietiestate25"
IMAGE="taekwondodev/dietiestate25-backend:latest"
ARCHIVE="/tmp/backend.tar"

echo "==> Pull immagine da Docker Hub..."
podman pull "$IMAGE"

echo "==> Salvataggio archivio locale..."
podman save "$IMAGE" -o "$ARCHIVE"

echo "==> Caricamento nel cluster Kind..."
KIND_EXPERIMENTAL_PROVIDER=podman kind load image-archive "$ARCHIVE" --name "$CLUSTER"

echo "==> Riavvio deployment..."
kubectl rollout restart deployment/backend -n "$CLUSTER"
kubectl rollout status deployment/backend -n "$CLUSTER"

echo "==> Aggiornamento completato."
