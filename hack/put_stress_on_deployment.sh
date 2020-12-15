#!/usr/bin/env bash

set -e

envs="SERVICE_NAME SERVICE_PORT"
for e in ${envs}; do
    [ -z "$(printenv $e)" ] && echo "Missing env: $e" && exit 1 || true
done



## Put some stress on deployment
## Add 30qps stress for 120s
DURATION=120
QPS=30

for (( i=1; i<=${DURATION}; i++ )); do
  for (( j=1; j<=${QPS}; j++ ));
  do
    curl -sL "http://`kubectl get svc | grep ${SERVICE_NAME} | awk '{print $4}'`:${SERVICE_PORT}/stress" > /dev/null &
  done
  sleep 1s
done