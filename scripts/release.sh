#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
export KUBECONFIG="${KUBECONFIG:-$HOME/.kube/config}"

version="${1:-v2}"
case "$version" in v1|v2|bad) ;; *) echo "Usage: bash scripts/release.sh v1|v2|bad"; exit 1;; esac
if kubectl --context kind-shoppingcart -n argocd get application shoppingcart >/dev/null 2>&1; then
  echo "GitOps is enabled. Change gitops/app/kustomization.yaml and push to Git instead."; exit 1
fi
python3 scripts/update-image.py shoppingcart "$version"
kubectl --context kind-shoppingcart apply -k gitops/app
kubectl --context kind-shoppingcart -n shoppingcart get rollout,analysisrun,pods
