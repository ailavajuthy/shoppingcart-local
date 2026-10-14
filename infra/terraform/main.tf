terraform {
  required_version = ">= 1.6.0, < 2.0.0"
  required_providers {
    kubernetes = { source = "hashicorp/kubernetes", version = "~> 2.36.0" }
    helm       = { source = "hashicorp/helm", version = "~> 2.17.0" }
  }
}
provider "kubernetes" {
  config_path = pathexpand(var.kubeconfig)
  config_context = "kind-shoppingcart"
}
provider "helm" {
  kubernetes {
    config_path = pathexpand(var.kubeconfig)
    config_context = "kind-shoppingcart"
  }
}
variable "kubeconfig" {
  type = string
  default = "~/.kube/config"
}
variable "db_password" {
  type = string
  sensitive = true
  default = "local-demo-password"
}
variable "grafana_password" {
  type = string
  sensitive = true
  default = "local-grafana-password"
}
resource "kubernetes_namespace_v1" "namespaces" {
  for_each = toset(["shoppingcart", "monitoring", "argocd", "argo-rollouts"])
  metadata { name = each.value }
}
resource "kubernetes_secret_v1" "database" {
  metadata {
    name = "db-credentials"
    namespace = kubernetes_namespace_v1.namespaces["shoppingcart"].metadata[0].name
  }
  data = { password = var.db_password }
}
resource "kubernetes_secret_v1" "grafana" {
  metadata {
    name = "grafana-credentials"
    namespace = kubernetes_namespace_v1.namespaces["monitoring"].metadata[0].name
  }
  data = { password = var.grafana_password }
}
locals {
  files = fileset("${path.module}/manifests", "*.yaml")
}
resource "kubernetes_manifest" "infrastructure" {
  for_each = local.files
  manifest = yamldecode(file("${path.module}/manifests/${each.value}"))
  depends_on = [kubernetes_namespace_v1.namespaces, kubernetes_secret_v1.database, kubernetes_secret_v1.grafana]
}
resource "helm_release" "argocd" {
  name = "argocd"
  repository = "https://argoproj.github.io/argo-helm"
  chart = "argo-cd"
  version = "7.7.16"
  namespace = kubernetes_namespace_v1.namespaces["argocd"].metadata[0].name
  timeout = 900
  values = [yamlencode({
    configs = { params = { "server.insecure" = true } }
    dex = { enabled = false }
    notifications = { enabled = false }
    applicationSet = { enabled = false }
  })]
}
resource "helm_release" "rollouts" {
  name = "argo-rollouts"
  repository = "https://argoproj.github.io/argo-helm"
  chart = "argo-rollouts"
  version = "2.38.2"
  namespace = kubernetes_namespace_v1.namespaces["argo-rollouts"].metadata[0].name
  timeout = 600
  values = [yamlencode({ dashboard = { enabled = true } })]
}
