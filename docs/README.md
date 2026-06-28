Create Topic
```
docker exec -it kafka kafka-topics \
--create \
--topic raw-logs-topic \
--bootstrap-server localhost:9092
```

To see exist topic
```
docker exec -it kafka kafka-topics \
--list \
--bootstrap-server localhost:9092
```

```
docker compose down
```

```
docker compose up --build -d
```

```
docker exec -it ai-log-postgres psql -U admin -d logdb -c "SELECT id, level, service, message, created_at FROM structured_logs;"
```

```
curl http://localhost:3000/health
curl -X POST http://localhost:8000/embed -H "Content-Type: application/json" -d "{\"text\": \"database connection failed\"}"
```

```
docker exec -it ai-log-postgres psql -U admin -d logdb -c "CREATE EXTENSION IF NOT EXISTS vector;"
docker exec -it ai-log-postgres psql -U admin -d logdb -c "SELECT id, level, service, embedding IS NOT NULL as has_embedding FROM structured_logs;"
```

```
docker exec -it ai-log-postgres psql -U admin -d logdb -c "CREATE INDEX ON structured_logs USING hnsw (embedding vector_cosine_ops);"
```

```
curl -X POST http://localhost:8080/logs -H "Content-Type: application/json" -d "{\"rawMessage\": \"2026-05-28 ERROR payment-service Database connection timeout after 30s\"}"

curl -X POST http://localhost:8080/logs -H "Content-Type: application/json" -d "{\"rawMessage\": \"2026-05-28 ERROR payment-service Failed to process payment for order 1234\"}"

curl -X POST http://localhost:8080/logs -H "Content-Type: application/json" -d "{\"rawMessage\": \"2026-05-28 WARN auth-service Login attempt failed for user harsh@test.com\"}"

curl -X POST http://localhost:8080/logs -H "Content-Type: application/json" -d "{\"rawMessage\": \"2026-05-28 INFO auth-service User harsh@test.com logged in successfully\"}"
```

```
curl -X POST http://localhost:8080/logs/query -H "Content-Type: application/json" -d "{\"question\": \"What is wrong with the payment service?\"}"
```
