#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
export KUBECONFIG="${KUBECONFIG:-$HOME/.kube/config}"

for tool in docker kind kubectl terraform python3 curl; do
  command -v "$tool" >/dev/null || { echo "Missing tool: $tool. Read START_HERE.md part 1."; exit 1; }
done
docker info >/dev/null
python3 --version
docker --version
kind version
kubectl version --client
terraform version
echo "Prerequisites found. Keep Docker running; allocate about 8 GB RAM and 4 CPUs."
