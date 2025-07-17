docker compose down
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up mysql -d --remove-orphans --build --force-recreate
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up phpmyadmin -d --remove-orphans --build --force-recreate
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up litespeed -d --remove-orphans --build --force-recreate
