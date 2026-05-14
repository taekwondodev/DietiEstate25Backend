#!/usr/bin/env bash
set -euo pipefail

CLUSTER="dietiestate25"
IMAGE="taekwondodev/dietiestate25-backend:latest"
ARCHIVE="/tmp/backend.tar"
DIR="$(dirname "$0")"

echo "==> Pull immagine da Docker Hub..."
podman pull "$IMAGE"

echo "==> Salvataggio archivio locale..."
podman save "$IMAGE" -o "$ARCHIVE"

echo "==> Caricamento nel cluster Kind..."
KIND_EXPERIMENTAL_PROVIDER=podman kind load image-archive "$ARCHIVE" --name "$CLUSTER"

echo "==> Applica manifesti..."
kubectl apply -f "$DIR/namespace.yaml"
kubectl apply -f "$DIR/postgres/"
kubectl apply -f "$DIR/backend/"

echo "==> Verifica stato Pod..."
kubectl get pods -n "$CLUSTER"

echo "==> Deploy completato."
