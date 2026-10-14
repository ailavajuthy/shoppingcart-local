#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
export KUBECONFIG="${KUBECONFIG:-$HOME/.kube/config}"

bash scripts/doctor.sh
if ! kind get clusters | grep -qx shoppingcart; then
  kind create cluster --name shoppingcart --config infra/kind/cluster.yaml
fi
kubectl config use-context kind-shoppingcart
bash scripts/build-images.sh
terraform -chdir=infra/terraform init
terraform -chdir=infra/terraform plan -out=tfplan
terraform -chdir=infra/terraform apply tfplan
kubectl --context kind-shoppingcart -n shoppingcart rollout status deployment/postgres --timeout=300s
kubectl --context kind-shoppingcart wait --for=condition=Established crd/rollouts.argoproj.io --timeout=120s
kubectl --context kind-shoppingcart apply -k gitops/app
kubectl --context kind-shoppingcart -n shoppingcart wait --for=jsonpath='{.status.phase}'=Healthy rollout/shoppingcart --timeout=600s
kubectl --context kind-shoppingcart -n monitoring rollout status deployment/prometheus --timeout=180s
kubectl --context kind-shoppingcart -n monitoring rollout status deployment/grafana --timeout=180s
curl --fail http://localhost:8080/health/ready
echo "Shopping cart: http://localhost:8080/login.jsp (demo@example.com / demo123)"
