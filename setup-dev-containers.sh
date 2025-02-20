docker compose down
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up mysql -d
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up phpadmin -d
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up litespeed -d
