#!/bin/bash

# Variables
CONTAINER1="heroes-of-penta-lp-node1"
CONTAINER2="heroes-of-penta-lp-node2"
IP1="172.16.17.2"
IP2="172.16.17.3"
ACTIVE_SERVICE=""
INACTIVE_SERVICE=""
COMPOSE_FILE="docker-compose.yaml"

# Step 1: Determine which service is active
if docker ps --format '{{.Names}}' | grep -q $CONTAINER1; then
  ACTIVE_SERVICE=$CONTAINER1
  INACTIVE_SERVICE=$CONTAINER2
elif docker ps --format '{{.Names}}' | grep -q $CONTAINER2; then
  ACTIVE_SERVICE=$CONTAINER2
  INACTIVE_SERVICE=$CONTAINER1
else
  echo "No active container - install first!"
  docker compose up -d --remove-orphans --build --force-recreate
  INACTIVE_CONTAINER=$(docker ps -f name=$CONTAINER2 -q | tail -n1)
  docker container stop $INACTIVE_CONTAINER
  exit 0
fi

echo "Active service: $ACTIVE_SERVICE"
echo "Inactive service: $INACTIVE_SERVICE"

# Step 2: Build and deploy the inactive service
echo "Building the new version of $INACTIVE_SERVICE..."
docker compose -f $COMPOSE_FILE build $INACTIVE_SERVICE

echo "Starting $INACTIVE_SERVICE..."
docker compose -f $COMPOSE_FILE up -d $INACTIVE_SERVICE

# TODO: verify that container actually started with a ping

# Step 3: Update config
ACTIVE_CONTAINER=$(docker ps -f name=$ACTIVE_SERVICE -q | tail -n1)
INACTIVE_CONTAINER=$(docker ps -f name=$INACTIVE_SERVICE -q | tail -n1)
ACTIVE_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $ACTIVE_CONTAINER)
INACTIVE_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $INACTIVE_CONTAINER)
echo "Active container IP: $ACTIVE_IP"
echo "Inactive container IP: $INACTIVE_IP"
sed -i -e "s/$ACTIVE_IP/$INACTIVE_IP/g" ./deploy/lsws/conf/vhosts/Example/vhconf.conf

bash deploy/bin/webadmin.sh --restart

# Step 4: Stop the active service
echo "Stopping $ACTIVE_SERVICE..."
docker compose -f $COMPOSE_FILE stop $ACTIVE_SERVICE

#docker exec -it heroes-of-penta-litespeed-1 systemctl restart lsws

echo "Deployment complete. $INACTIVE_SERVICE is now active."


# reload_nginx() {  
#   docker exec nginx /usr/sbin/nginx -s reload  
# }

# zero_downtime_deploy() {  
#   service_name=heroes-of-penta-heroes-of-penta-lp-node
#   old_container_id=$(docker ps -f name=$service_name -q | tail -n1)

#   # bring a new container online, running new code  
#   # (nginx continues routing to the old container only)  
#   docker compose up -d --no-deps --scale $service_name=2 --no-recreate $service_name

#   # wait for new container to be available  
#   new_container_id=$(docker ps -f name=$service_name -q | head -n1)
#   new_container_ip=$(docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $new_container_id)
#   curl --silent --include --retry-connrefused --retry 30 --retry-delay 1 --fail http://$new_container_ip:3000/ || exit 1

#   # start routing requests to the new container (as well as the old)  
#   reload_nginx

#   # take the old container offline  
#   docker stop $old_container_id
#   docker rm $old_container_id

#   docker compose up -d --no-deps --scale $service_name=1 --no-recreate $service_name

#   # stop routing requests to the old container  
#   reload_nginx  
# }

# zero_downtime_deploy