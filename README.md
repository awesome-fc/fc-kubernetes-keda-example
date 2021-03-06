# fc-kubernetes-keda-example

In this tutorial, we will show you how to build your own custom-container runtime Image in Java, and then you can deploy your runtime using docker only, or on your own kubernetes cluster, which also has event driven ability provided by [KEDA](https://keda.sh/), or on [Alibaba Cloud Function Compute](https://fc.console.aliyun.com/).

## Before you begin

+ Install [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/).
+ Use a cloud provider like [Alibaba Cloud Container Service](https://cs.console.aliyun.com) to create a Kubernetes cluster or not if you already have one.
+ Make sure you `kubectl` tool is correctly configured. You can verify it with `kubectl version`.
+ Install [KEDA](https://keda.sh/docs/2.0/deploy/) on you cluster.
+ Install [funcraft](https://github.com/alibaba/funcraft/blob/master/docs/usage/installation.md). This is optional if you want to deploy your function on Alibaba Cloud in this tutorial.


## Step 1: Build and push your custom-container runtime

```bash
git clone git@github.com:awesome-fc/fc-kubernetes-keda-example.git

# Customize your own image name, e.g. registry.cn-shenzhen.aliyuncs.com/my-fc-demo/java-springboot:1.0
export FC_DEMO_IMAGE="registry.cn-shenzhen.aliyuncs.com/my-fc-demo/java-springboot:1.0"

docker build -t ${FC_DEMO_IMAGE} .

# Docker login before pushing, replace {your-ACR-registry}, e.g. registry.cn-shenzhen.aliyuncs.com
# It's OK if you want to push your image to your dedicated registry.
# Make sure your Kubernetes cluster has access to your registry.
docker login registry.cn-shenzhen.aliyuncs.com

# Push the image
docker push ${FC_DEMO_IMAGE}

```


## Step 2: Deploy your custom-container runtime using docker only locally

```bash
# Local test
docker run -p 8080:8080 ${FC_DEMO_IMAGE}

curl localhost:8080/2016-08-15/proxy/CustomContainerDemo/java-springboot-http/

```


## Step3: Deploy your custom-container runtime on Kubernetes Cluster with KEDA

```bash
# Customize your own deployment name, e.g. demo-java-springboot
export DEPLOYMENT_NAME=demo-java-springboot
# In this case, container port must be 8080.
export CONTAINER_PORT=8080
# Customize service port, e.g. 80
export SERVICE_PORT=80

export SERVICE_NAME=${DEPLOYMENT_NAME}-svc

# Create deployment
# Expose your deployment. WARNING: it will cost you some credit.
/bin/bash ./hack/deploy_to_kubernetes.sh

# Verify your deployment is available.
curl -L "http://`kubectl get svc | grep ${SERVICE_NAME} | awk '{print $4}'`:${SERVICE_PORT}/2016-08-15/proxy/CustomContainerDemo/java-springboot-http/"

# Customize your own ScaledObject name, e.g. cron-scaled-obj
export SCALED_OBJECT_NAME=cron-scaled-obj

# Create ScaleObject
/bin/bash ./hack/create_cpu_cron_scaledobject.sh

## Deployment replicas will be 5 between 15 and 30 every hour
kubectl get deployments.apps ${DEPLOYMENT_NAME}


## Put some stress on deployment
## Add 30qps stress for 120s
/bin/bash ./hack/put_stress_on_deployment.sh 2>&1 > /dev/null &

## We will see all pods' CPU usage are increasing
## And pods count will reach limit in a while
kubectl top pod | grep ${DEPLOYMENT_NAME}

```


## Step 4: Deploy your custom-container runtime to Alibaba Cloud Function Compute

```bash
# Set FC_DEMO_IMAGE to your ACR image, e.g. registry-vpc.cn-shenzhen.aliyuncs.com/{your-namespace}/fc-demo-java-spring-boot:v1
export FC_DEMO_IMAGE={your_image}

# Substitute {FC_DEMO_IMAGE} in template.yml
./setup.sh

# Configure funcraft, make sure the container registry and fun are in the same region, skip this step if fun is already configured.
fun config

# Build the Docker image
fun build --use-docker

# Deploy the function, push the image via the internet registry host (the function config uses the VPC registry for faster image pulling)
fun deploy --push-registry acr-internet

# After a successful deploy, fun should return a HTTP proxy URL to invoke the function
curl https://{your-account-id}.{region}.fc.aliyuncs.com/2016-08-15/proxy/CustomContainerDemo/java-springboot-http/

```

