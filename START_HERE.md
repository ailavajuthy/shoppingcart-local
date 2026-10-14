# Shopping Cart: local progressive delivery project

This package contains your shopping-cart source plus a local DevOps pipeline. Unzip it into a **new folder**, open `shoppingcart-local` in VS Code or IntelliJ, and run commands in that folder's terminal. Do not paste these files into individual IntelliJ classes: every file is already in its required location.

No AWS account, EKS cluster, Terraform AWS provider, or paid cloud deployment is used. GitHub/GHCR is optional until part 4. Local Kubernetes does not prove EKS portability, performance or cost equivalence; update your project proposal to describe local comparison validation.

**Validation status:** configuration parsing, shell syntax and Python syntax have been checked here. Docker, Terraform, Kubernetes and Maven dependency downloads are unavailable in this execution environment, so the container build and end-to-end deployment still need to run on your laptop. The scripts below are your runtime acceptance checks. No benchmark results are invented.

## Part 1 of 5: put the project on your laptop and test the container

1. Extract the ZIP somewhere convenient, such as `~/Documents/shoppingcart-local`.
2. Open that folder in your editor. `pom.xml`, `Dockerfile`, `compose.yaml` and this guide must be at the top level.
3. Install Docker Desktop and start it. For this full lab, allow about 8 GB RAM, 4 CPU cores, and 15 GB disk space. First-time image/chart downloads require internet access. An older macOS installation may need an OS upgrade to support your chosen Docker Desktop release.
4. Install Git, Python 3, kubectl, kind and Terraform. You do not need to install Java/Maven separately: Docker builds with Java 17 and Maven.

On macOS with Homebrew already installed:

```bash
brew install git python kind kubectl
brew tap hashicorp/tap
brew install hashicorp/tap/terraform
```

On Windows, use Docker Desktop with WSL2 and run the commands inside an Ubuntu WSL terminal. Install the Linux versions of kind, kubectl and Terraform from their official installation pages. On Linux, use Docker Engine and the same command-line tools.

Open a terminal inside the extracted folder:

```bash
cd ~/Documents/shoppingcart-local
bash scripts/doctor.sh
# First verify the app and PostgreSQL without Kubernetes:
docker compose up --build -d
curl --fail http://localhost:8080/health/ready
```

Open **http://localhost:8080/login.jsp**. Login: `demo@example.com`, password: `demo123`. Browse products, add one to the cart, check out, and inspect order history. You can also register a new demo account.

Stop Compose before starting Kubernetes, because both use port 8080:

```bash
docker compose down
```

The Compose database is separate from the Kubernetes database. Data entered here does not transfer automatically. `docker compose down` preserves its volume; `docker compose down -v` deletes this disposable demo database.

### Where the code belongs

| File/folder | Purpose | Where to use it |
|---|---|---|
| `src/main/java/` | Your Java servlets, DAOs and added health/auth/metrics code | Already inside this Maven project; edit in IntelliJ/VS Code |
| `src/main/webapp/` | Your JSP screens and CSS | Already inside the application |
| `pom.xml` | Java 17, Servlet 6.0 and WAR build | Project root; Docker invokes Maven |
| `Dockerfile`, `.dockerignore` | Multi-stage, non-root Tomcat image | Project root; `docker build` reads these |
| `database/001-init.sql` | Reconstructed schema, functions and seed data | PostgreSQL runs this automatically on a new database volume |
| `compose.yaml` | First app + database checkpoint | Run `docker compose` from the root |
| `infra/kind/cluster.yaml` | Local cluster and localhost port mappings | Read automatically by `scripts/local-up.sh` |
| `infra/terraform/main.tf` | Namespaces, secrets, Helm controllers and resource definitions | Terraform runs in `infra/terraform/` |
| `infra/terraform/manifests/` | Database, monitoring, RBAC and dashboard config | Managed by Terraform, not Argo CD |
| `gitops/app/` | Rollout, Services, metric analysis and demo traffic | Managed by kubectl locally, then Argo CD in GitOps mode |
| `.github/workflows/build.yaml` | Build, push to GHCR, commit the new image tag | GitHub executes this after the project is pushed |
| `tests/` | Conventional Deployment and comparison runner | Run from the root after the local cluster is healthy |
| `scripts/` | Setup, releases, chaos and evidence commands | Run from the root |
| `docs/dashboard.json` | Grafana dashboard source | Dashboard is provisioned automatically |

The ZIP did not include SQL definitions. `001-init.sql` is a new compatible implementation based on the DAO queries, not a recovered copy of your original database. Do not apply it to a real existing database without adapting it.

## Part 2 of 5: create the local Kubernetes infrastructure

Run:

```bash
bash scripts/local-up.sh
```

The script creates a kind cluster named `shoppingcart`, builds `v1`, `v2` and `bad` images, loads them into kind, runs Terraform plan/apply, installs Argo CD and Argo Rollouts, starts PostgreSQL/Prometheus/Grafana, and applies the app resources. Allow time for the first downloads.

It pins Kubernetes operations to `kind-shoppingcart`. Terraform provisions resources **inside** the cluster; kind creates the cluster itself. This setup does not provision an AWS VPC or managed control plane.

Expected checks:

```bash
kubectl --context kind-shoppingcart -n shoppingcart get pods
kubectl --context kind-shoppingcart -n shoppingcart get rollout shoppingcart
curl --fail http://localhost:8080/health/ready
curl --fail http://localhost:8080/demo/products
```

The Rollout should be `Healthy`; `/demo/products` should return a positive product count. The first installation establishes a baseline and skips canary analysis; metric checks occur on subsequent versions.

Open the same login URL as part 1. Kubernetes starts with its own demo database. Authentication tokens are stored in PostgreSQL so login works across replicas. Transient flash messages remain in each Tomcat session.

Terraform state is stored locally; keep it while the cluster exists. Do not commit state, `terraform.tfvars`, or credentials to Git. The provided passwords and app authentication are for a local student demo. Your original plaintext-password behavior remains; do not use real passwords or publish this app as a production store.

If you edit `database/001-init.sql`, also update the copy in `infra/terraform/manifests/db-init.yaml`. Initialization scripts run only on empty database storage, so updating a ConfigMap does not migrate an existing database.

## Part 3 of 5: monitor, release and prove rollback

Use a separate terminal for each port-forward; leave each command running:

```bash
# Terminal A: Grafana
kubectl --context kind-shoppingcart -n monitoring port-forward svc/grafana 3000:3000
```

```bash
# Terminal B: Prometheus
kubectl --context kind-shoppingcart -n monitoring port-forward svc/prometheus 9090:9090
```

```bash
# Terminal C: Argo CD
kubectl --context kind-shoppingcart -n argocd port-forward svc/argocd-server 8088:80
```

| UI | Address | Login |
|---|---|---|
| Shopping cart | http://localhost:8080/login.jsp | `demo@example.com` / `demo123` |
| Grafana | http://localhost:3000 | `admin` / `local-grafana-password` |
| Prometheus | http://localhost:9090 | None in this local demo |
| Argo CD | http://localhost:8088 | `admin` / initial password command below |
| Stable-only app | http://localhost:8082/login.jsp | Same demo account |

Retrieve the initial Argo CD password:

```bash
kubectl --context kind-shoppingcart -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 --decode
```

Open Grafana's **Shopping Cart Progressive Delivery** dashboard. In Prometheus, open Targets: four shoppingcart pods should be scraped. Open Alerts to inspect `ShoppingCartHighErrorRate`.

Test a healthy release:

```bash
bash scripts/release.sh v2
kubectl --context kind-shoppingcart -n shoppingcart get rollout,analysisrun,pods -w
```

Press Ctrl+C once the rollout is Healthy. Allow roughly 5-6 minutes for all three analysis stages and startup. The Rollout goes through approximate 25%, 50%, and 100% candidate shares. Each stage waits 60 seconds, then checks three samples 20 seconds apart. Each sample requires at least 20 requests in the previous minute and less than 5% HTTP 5xx errors. Missing or insufficient traffic fails conservatively.

The `demo-traffic` Deployment sends steady stateless database-read requests to both the mixed service and the candidate-only service. Candidate-only traffic is diagnostic traffic; it lets analysis measure the candidate rather than dilute its errors with healthy pods. It is separate from the user-side comparison samples.

Test automatic rollback:

```bash
bash scripts/release.sh bad
kubectl --context kind-shoppingcart -n shoppingcart get rollout,analysisrun,pods -w
```

The bad image starts normally and passes readiness, but returns 503 on business requests. The metric analysis should fail at the first stage, the Rollout should become `Degraded`, and stable capacity should serve the mixed service again. This proves a metric-based abort, not just failure of a readiness probe. Inspect the failed AnalysisRun and capture the Grafana error spike and firing alert while the fault is active.

```bash
kubectl --context kind-shoppingcart -n shoppingcart describe rollout shoppingcart
kubectl --context kind-shoppingcart -n shoppingcart get analysisrun
curl --fail http://localhost:8082/demo/products
bash scripts/evidence.sh
```

A rollback restores serving capacity; it does **not** rewrite the desired image in Git. `Degraded` is expected after an aborted release. In local mode, return the desired state to healthy:

```bash
bash scripts/release.sh v2
```

For a separate pod-recovery experiment:

```bash
bash scripts/chaos-pod.sh
```

Watch the replacement pod appear, then Ctrl+C. A killed pod demonstrates controller self-healing; it is not by itself proof of metric-based release rollback.

## Part 4 of 5: enable GitHub Actions and Argo CD

Do part 5's local comparison before enabling GitOps, or use its documented pause procedure.

1. Create an empty **public** GitHub repository named `shoppingcart-local`. Use demo data only. A public repo avoids adding Argo CD repository credentials for this lab.
2. Run these commands from the project root, replacing `YOUR_USERNAME`:

```bash
git init -b main
git add .
git commit -m "Add local progressive delivery shopping cart"
git remote add origin https://github.com/YOUR_USERNAME/shoppingcart-local.git
git push -u origin main
```

3. In GitHub, open Settings -> Actions -> General -> Workflow permissions and allow Read and write permissions. The workflow needs to commit an image update to `main`; branch protection must permit it, or use a PR-based update workflow instead. If the first run failed before this setting, rerun the workflow.
4. Wait for the Actions run to finish. It builds Linux amd64/arm64 images, publishes to `ghcr.io/your_username/shoppingcart-local:COMMIT_SHA`, then commits the tag to `gitops/app/kustomization.yaml`.
5. Open your published container package in GitHub Packages and set its visibility to **Public**. Public repo visibility does not necessarily make its GHCR package public automatically. The cluster must be able to pull it without credentials for this lab.
6. Pull the image-update commit locally, then register the application:

```bash
git pull --rebase
python3 scripts/enable-gitops.py https://github.com/YOUR_USERNAME/shoppingcart-local.git
```

In Argo CD, the `shoppingcart` application should become Synced. The first remote image change runs through the canary checks. Docker/kind must stay running on your laptop; GitHub Actions never connects directly to your local cluster. Argo CD inside the cluster pulls the public Git repository.

From now on, edit Java/JSP code, commit and push. CI publishes a new SHA-tagged image and Argo CD deploys the tag committed by CI. GitOps paths are excluded from the workflow push trigger to avoid a build loop.

For a faulty GitOps release, open Actions -> Build and publish shopping cart -> Run workflow and enable the fault checkbox. After the failed rollout, run it again with fault disabled to publish a fresh healthy image, or revert the image tag in Git to the previous healthy SHA. Commit and push that correction. Do not use `scripts/release.sh` while GitOps owns the application.

## Part 5 of 5: compare strategies, collect evidence and clean up

The comparison is **normal Kubernetes rolling update versus metrics-gated canary**, both on the same local cluster and using the same shopping-cart image/database. It does not compare local Kubernetes against AWS.

In local mode with healthy infrastructure and all three images loaded:

```bash
python3 tests/compare.py
```

This runs roughly six minutes plus setup/recovery time. It creates a separate four-replica rolling Deployment on port 8081. For each strategy, it measures a 15-second healthy baseline, deploys the bad image, then continues the same four-request-per-second `/demo/products` workload. A new client connection is used for each sample. The rolling Deployment has readiness probes but no metrics-based rollback; the canary has metric gates.

The script restores healthy images afterward. Output is placed in `results/comparison-TIMESTAMP/`:

| Output | What it shows |
|---|---|
| `rolling-requests.csv`, `canary-requests.csv` | Timestamp, HTTP status, served version, latency and whether the change occurred |
| `summary.csv` | Failed requests, error %, mean/p95 successful-request latency and observed recovery |
| `rollout.json` | Rollout status before restoring the candidate image |
| `analysisruns.json` | Analysis measurements and pass/fail evidence |

Recovery is measured from candidate submission until the first successful request after the final observed failure, provided at least 20 subsequent samples also succeed. This is an observed client recovery measure, not an exact controller abort timestamp. If recovery is not seen during the window, the output says `not observed`.

Repeat at least three times and report the actual results, including variation. Keep the sample rate, test length and images identical. The client runs sequentially, so achieved throughput can fall during slow requests. Successful-request latency excludes failed requests; failures remain in the error metric. For fair interpretation, disclose that canary also has synthetic diagnostic traffic and the trials share a cluster/database. These are educational sequential trials, not a statistically controlled production load benchmark.

The normal rolling Deployment is expected to serve a large fraction of failures after full replacement; the canary is expected to abort and recover. Those are hypotheses until your CSV and failed AnalysisRun confirm them.

If GitOps was already enabled, pause application ownership by deleting **only** the Application resource (the provided Application has no cascading-delete finalizer):

```bash
kubectl --context kind-shoppingcart -n argocd delete application shoppingcart
python3 tests/compare.py
# Reattach GitOps after testing; it restores the desired version from Git:
python3 scripts/enable-gitops.py https://github.com/YOUR_USERNAME/shoppingcart-local.git
```

Take screenshots of the successful build, Argo CD sync, dashboard, failed AnalysisRun, restored stable service, and CSV results. Run `bash scripts/evidence.sh` while the desired evidence is present.

Cleanup, after saving results/screenshots:

```bash
bash scripts/local-down.sh
```

Terraform asks you to confirm destruction. The kind cluster's demo database and in-memory monitoring history will be deleted. Local `results/` files remain.

### Troubleshooting

| Symptom | What to check |
|---|---|
| Port 8080 occupied | Stop Compose; close any other server using 8080 |
| Docker connection error | Start Docker Desktop/Engine |
| `Pending` pods / Terraform timeout | Docker CPU/RAM, `kubectl describe pod`, and available disk space |
| `ImagePullBackOff` remotely | GHCR package visibility; image name/tag committed by CI |
| Local image cannot be found | Run `bash scripts/build-images.sh`; ensure context `kind-shoppingcart` |
| Database table/function missing | Init SQL runs only on new volumes; inspect PostgreSQL logs |
| Rollout fails with a healthy image | Check Prometheus Targets, `demo-traffic` logs and DB availability; insufficient samples fail the gate |
| Argo CD repeatedly edits Service selectors | Keep the hash-selector ignore rules in `gitops/application.example.yaml` |
| Argo CD is OutOfSync | Public repo URL/branch/path, CI tag commit, package pull access |
| Workflow cannot push | Actions write permission and branch protection settings |

Useful commands:

```bash
kubectl --context kind-shoppingcart -n shoppingcart logs deployment/postgres
kubectl --context kind-shoppingcart -n shoppingcart logs deployment/demo-traffic
kubectl --context kind-shoppingcart -n shoppingcart describe rollout shoppingcart
kubectl --context kind-shoppingcart -n monitoring logs deployment/prometheus
kubectl --context kind-shoppingcart -n shoppingcart get events --sort-by=.metadata.creationTimestamp
```

### Architecture and report wording

Build flow: Git push -> GitHub Actions -> GHCR image -> Git manifest commit -> Argo CD -> Argo Rollouts. Measurement flow: application counters -> Prometheus -> AnalysisRun decision and Grafana panels. Terraform owns the database, namespaces, monitoring and controller installations; Argo CD owns application delivery.

This small canary uses replica counts without a traffic router. The 25/50/100 shares are approximate, affected by surge pods, connection reuse and Kubernetes routing. Do not claim exact traffic weights. PostgreSQL is one replica and is not a highly available database. Prometheus/Grafana history is disposable. Alerts fire in Prometheus; this package does not configure email or Slack notifications.

Suggested report scope: "We containerized the Shopping Cart application and evaluated normal rolling updates against Prometheus-driven canary delivery on a local kind Kubernetes cluster. Terraform managed supporting resources, GitHub Actions built images, and Argo CD reconciled application manifests. Deliberately faulty releases tested automated rollback without paid AWS resources."

Official references used for design:
- https://argo-rollouts.readthedocs.io/en/stable/features/analysis/
- https://argo-rollouts.readthedocs.io/en/stable/features/canary/
- https://prometheus.io/docs/prometheus/latest/configuration/configuration/
- https://tomcat.apache.org/whichversion.html
- https://kind.sigs.k8s.io/docs/user/quick-start/
- https://developer.hashicorp.com/terraform/install
