#!/bin/bash

GREEN='\033[0;32m'
NC='\033[0m'

start_time=$(date +%s)

# Step 1: Build util-service
cd ./util-service && mvn clean install && cd ..

# Step 2: Build all Maven projects
echo -e "${GREEN}[UPDATING PROJECT JARS]${NC}"
for f in ./*; do [ -d "$f" ] && [ -f "$f/pom.xml" ] && (cd "$f" && mvn clean install); done

# Step 3: Build Docker Compose services
echo -e "${GREEN}[UPDATING DOCKER CONTAINERS]${NC}"
docker compose build
docker compose up mongodb mysql kafka zookeeper kafka-ui -d --build

# Step 4: Load Docker images into Kind cluster
echo -e "${GREEN}[LOAD IMAGES TO THE CLUSTER]${NC}"
IMAGES=(
  "microservice-landscape/review:latest"
  "microservice-landscape/product:latest"
  "microservice-landscape/composite:latest"
  "microservice-landscape/auth-server:latest"
  "microservice-landscape/discovery:latest"
  "microservice-landscape/config-server:latest"
  "microservice-landscape/gateway:latest"
)
for image in "${IMAGES[@]}"; do
  kind load docker-image "$image" --name=dev-cluster-multi
done

# Step 5: Update Helm dependencies
echo -e "${GREEN}[UPDATING HELM DEPENDENCIES]${NC}"
for f in kubernetes/helm/components/*; do helm dep up $f; done && for f in kubernetes/helm/environments/*; do helm dep up $f; done

# Step 6: Uninstall and reinstall the Helm release
echo -e "${GREEN}[RE INSTALLING HELM RELEASE]${NC}"
helm uninstall landscape-dev-env || true
helm install landscape-dev-env kubernetes/helm/environments/dev-env


end_time=$(date +%s)
duration=$((end_time - start_time))
echo -e "${GREEN}Script executed in $duration seconds.${NC}"

#7 Step 7: Wait for pods to be ready
echo -e "${GREEN}[WAITING FOR PODS TO BE READY]${NC}"
kubectl wait --timeout=600s --for=condition=ready pod --all

