#!/bin/bash

GREEN='\033[0;32m'
NC='\033[0m'

start_time=$(date +%s)

# Check if a service name is provided
if [ -z "$1" ]; then
  echo -e "${GREEN}Please provide a service name.${NC}"
  exit 1
fi

SERVICE_NAME=$1

# Step 1: Build the specified service
if [ -d "./$SERVICE_NAME-service" ] && [ -f "./$SERVICE_NAME-service/pom.xml" ]; then
  echo -e "${GREEN}[BUILDING $SERVICE_NAME]${NC}"
  cd "./$SERVICE_NAME-service" && mvn clean install && cd ..
else
  echo -e "${GREEN}Service $SERVICE_NAME not found or does not have a pom.xml.${NC}"
  exit 1
fi

# Step 2: Build Docker Compose services
echo -e "${GREEN}[UPDATING DOCKER CONTAINERS]${NC}"
docker compose build "$SERVICE_NAME"

# Step 3: Load Docker images into Kind cluster for the specific service
echo -e "${GREEN}[LOAD IMAGES TO THE CLUSTER]${NC}"
IMAGE="microservice-landscape/$SERVICE_NAME:latest"
kind load docker-image "$IMAGE" --name=dev-cluster-multi

# Step 4: Update Helm dependencies for the specific service
echo -e "${GREEN}[UPDATING HELM DEPENDENCIES]${NC}"
HELM_DIR="kubernetes/helm/components/$SERVICE_NAME"
if [ -d "$HELM_DIR" ]; then
  helm dep up "$HELM_DIR"
else
  echo -e "${GREEN}Helm directory for $SERVICE_NAME not found.${NC}"
  exit 1
fi

# Step 5: Upgrade the Helm release for the specific service
echo -e "${GREEN}[UPDATING HELM RELEASE]${NC}"
helm upgrade landscape-dev-env kubernetes/helm/environments/dev-env
kubectl rollout restart deployment $SERVICE_NAME

end_time=$(date +%s)
duration=$((end_time - start_time))
echo -e "${GREEN}Script executed in $duration seconds.${NC}"

# Step 6: Wait for pods to be ready for the specific service
echo -e "${GREEN}[WAITING FOR PODS TO BE READY]${NC}"
kubectl wait --timeout=300s --for=condition=ready pod -l app="$SERVICE_NAME"
