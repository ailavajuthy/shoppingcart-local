#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
export KUBECONFIG="${KUBECONFIG:-$HOME/.kube/config}"

out="results/evidence-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$out"
kubectl --context kind-shoppingcart -n shoppingcart get rollout,analysisrun,replicaset,pods,services -o yaml > "$out/workloads.yaml"
kubectl --context kind-shoppingcart -n shoppingcart describe rollout shoppingcart > "$out/rollout.txt"
kubectl --context kind-shoppingcart -n shoppingcart get events --sort-by=.metadata.creationTimestamp > "$out/events.txt"
kubectl --context kind-shoppingcart -n shoppingcart get analysisrun -o json > "$out/analysis.json"
curl --fail --silent http://localhost:9090/api/v1/alerts > "$out/alerts.json" || echo "Start the Prometheus port-forward to save alerts."
echo "Evidence saved in $out. Also capture the Grafana dashboard and Argo CD UI."
