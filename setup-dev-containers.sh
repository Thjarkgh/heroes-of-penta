docker compose down
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up mysql -d --build --force-recreate
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up phpmyadmin -d --build --force-recreate
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up litespeed -d --build --force-recreate
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml build heroes-of-penta-lp-node1 --pull --no-cache
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up heroes-of-penta-lp-node1 -d
