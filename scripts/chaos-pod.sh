#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
export KUBECONFIG="${KUBECONFIG:-$HOME/.kube/config}"

pod=$(kubectl --context kind-shoppingcart -n shoppingcart get pod -l app=shoppingcart -o jsonpath='{.items[0].metadata.name}')
echo "Deleting application pod $pod (database is left running)."
kubectl --context kind-shoppingcart -n shoppingcart delete pod "$pod"
kubectl --context kind-shoppingcart -n shoppingcart get pods -w
