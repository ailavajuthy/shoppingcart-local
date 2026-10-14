#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
export KUBECONFIG="${KUBECONFIG:-$HOME/.kube/config}"

echo "Deleting the shoppingcart cluster removes its demo database and metrics."
# Remove application resources first so Terraform can remove the controllers safely.
kubectl --context kind-shoppingcart -n argocd delete application shoppingcart --ignore-not-found
kubectl --context kind-shoppingcart delete -k gitops/app --ignore-not-found
terraform -chdir=infra/terraform destroy
kind delete cluster --name shoppingcart
