#!/usr/bin/env bash

set -e

envs="FC_DEMO_IMAGE DEPLOYMENT_NAME SERVICE_NAME SERVICE_PORT CONTAINER_PORT"
for e in ${envs}; do
    [ -z "$(printenv $e)" ] && echo "Missing env: $e" && exit 1 || true
done

# Create deployment
kubectl apply -f - <<-EOF
apiVersion: apps/v1 # for versions before 1.9.0 use apps/v1beta2
kind: Deployment
metadata:
  name: ${DEPLOYMENT_NAME}
spec:
  selector:
    matchLabels:
      app: ${DEPLOYMENT_NAME}
  replicas: 1 # instances count starts with 1
  template:
    metadata:
      labels:
        app: ${DEPLOYMENT_NAME}
    spec:
      containers:
      - name: ${DEPLOYMENT_NAME}
        image: ${FC_DEMO_IMAGE}
        resources:
          limits:
            cpu: 500m
          requests:
            cpu: 200m
EOF

# Expose your deployment. WARNING: it will cost you some credit.
kubectl expose deployment ${DEPLOYMENT_NAME} --port=${SERVICE_PORT} --target-port=${CONTAINER_PORT} --type=LoadBalancer --name=${SERVICE_NAME}