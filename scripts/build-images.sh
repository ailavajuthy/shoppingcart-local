#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
export KUBECONFIG="${KUBECONFIG:-$HOME/.kube/config}"

for version in v1 v2 bad; do
  fault=false
  if [ "$version" = bad ]; then fault=true; fi
  docker build --build-arg APP_VERSION="$version" --build-arg APP_FAULT="$fault" -t "shoppingcart:$version" .
done
kind load docker-image shoppingcart:v1 shoppingcart:v2 shoppingcart:bad --name shoppingcart
