# Gateway Kubernetes Manifests

This folder contains baseline Kubernetes manifests for deploying the gateway service.

## Included resources

- `namespace.yaml` - `kanokna` namespace
- `serviceaccount.yaml` - dedicated ServiceAccount for gateway pods
- `deployment.yaml` - rolling deployment with probes and resource limits
- `service.yaml` - ClusterIP service on port `80 -> 8080`
- `networkpolicy.yaml` - ingress/egress policy for gateway pods
- `kustomization.yaml` - apply entrypoint

## Apply

```bash
kubectl apply -k backend/windows-store-server/deployment/k8s/gateway
```

## Probe endpoints

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`
- Startup: `/actuator/health`
