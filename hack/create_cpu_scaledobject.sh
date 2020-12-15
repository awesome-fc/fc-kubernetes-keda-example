#!/usr/bin/env bash

set -e

envs="SCALED_OBJECT_NAME DEPLOYMENT_NAME"
for e in ${envs}; do
    [ -z "$(printenv $e)" ] && echo "Missing env: $e" && exit 1 || true
done

# Create ScaleObject with CPU trigger
kubectl apply -f - <<-EOF
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
metadata:
  name: ${SCALED_OBJECT_NAME}
spec:
  scaleTargetRef:
    name: ${DEPLOYMENT_NAME}
  minReplicaCount: 0
  maxReplicaCount: 10
  triggers:
  - type: cpu
    metadata:
      type: Utilization
      value: "50"
EOF