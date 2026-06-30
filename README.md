# AI Log Insight

AI-powered log analysis platform. Ingest logs, search them by meaning instead of just keywords, and ask questions about them.

```
POST /logs/query
{ "question": "What is wrong with the payment service?" }

→ "The payment-service is experiencing repeated database connection
   timeouts after 30s, which are causing downstream payment processing
   failures for order #1234."
```

---

## Architecture

![AI Log Insight HLD](docs/hld.svg)

**1. Log Ingestion** — Any service publishes raw log lines to the `raw-logs` Kafka topic.

**2. Log Processing (Spring Boot)** — `KafkaLogConsumer` consumes each message:
`KafkaLogConsumer → LogParserService → EmbeddingClient → LogUpdateService`
- `LogParserService` extracts `timestamp`, `level`, `service`, `message` via regex
- `EmbeddingClient` calls the embedding microservice for a 384-dim vector
- `LogUpdateService` saves the row, then writes the embedding via a native pgvector query

**3. Data Store** — PostgreSQL + pgvector. `structured_logs` table with `embedding vector(384)`, indexed with HNSW.

**4. Query Flow** — `POST /logs/query` → embed the question → SQL filter + pgvector cosine similarity → top-K relevant logs → prompt → Gemini → answer returned with the supporting logs.

---

## Tech Stack

| Layer | Technology |
|---|---|
| API | Spring Boot 4.x, Spring MVC |
| Message broker | Apache Kafka |
| Embeddings | FastAPI + sentence-transformers (`all-MiniLM-L6-v2`) |
| Vector DB | PostgreSQL + pgvector (HNSW index) |
| ORM | Spring Data JPA + Hibernate |
| LLM | Gemini 2.5 Flash |
| Infrastructure | Docker Compose |
| Testing | JUnit 5 + Mockito |

---

## Project Structure

```
ai_log_insight/
├── log-parser-service/
│   └── src/main/java/log_parser_service/
│       ├── controller/
│       │   └── LogController.java      # POST /logs, GET /logs, POST /logs/query
│       ├── service/
│       │   ├── LogParserService.java
│       │   ├── KafkaLogConsumer.java
│       │   ├── LogUpdateService.java
│       │   ├── EmbeddingClient.java
│       │   ├── EmbeddingService.java
│       │   ├── LlmClient.java
│       │   ├── GeminiLlmClient.java
│       │   └── LogQueryService.java
│       ├── repository/
│       │   └── StructuredLogRepository.java
│       ├── model/
│       │   └── StructuredLog.java
│       └── dto/
│           ├── RawLogRequest.java
│           ├── LogQueryRequest.java
│           └── LogQueryResponse.java
├── embedding-service/
│   ├── main.py
│   ├── Dockerfile
│   └── requirements.txt
├── init-db.sql
├── init-db.bat / init-db.sh
├── docker-compose.yml
└── docs/
    └── hld.svg
```

---

## Quick Start

### Prerequisites
- Java 21, Maven 3.9+
- Docker Desktop
- A Gemini API key (free at aistudio.google.com)

### 1. Start infrastructure
```bash
docker compose up --build -d
```
First run downloads the embedding model during the build (~5–10 min). Subsequent runs are instant.

### 2. Initialize the database
```bash
# Windows
init-db.bat
# Mac/Linux
./init-db.sh
```

### 3. Create the Kafka topic
```bash
docker exec -it kafka kafka-topics --create --topic raw-logs --bootstrap-server localhost:9092
```

### 4. Configure
```properties
# application.properties
gemini.api-key=YOUR_KEY_HERE
gemini.api-url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
```

### 5. Run
```bash
cd log-parser-service
./mvnw spring-boot:run
```

---

## API Reference

**Ingest a log**
```bash
POST /logs
{ "rawMessage": "2024-03-15 09:12:03 ERROR payment-service DB timeout after 30s" }
→ 202 Accepted
```

**Retrieve logs**
```bash
GET /logs
GET /logs?level=ERROR
GET /logs?service=payment-service
```

**Stats**
```bash
GET /logs/stats
→ { "total": 1024, "byLevel": { "ERROR": 42, "WARN": 13, "INFO": 969 } }
```

**AI query**
```bash
POST /logs/query
{
  "question": "What is causing payment failures?",
  "level": "ERROR",            // optional
  "service": "payment-service" // optional
}

→ {
  "question": "What is causing payment failures?",
  "answer": "The payment-service is experiencing database connection
             timeouts after 30s...",
  "logsAnalyzed": 8,
  "retrievedLogs": [ ... ]
}
```

---

## Running Tests

```bash
cd log-parser-service
./mvnw test
```
```
LogParserServiceTest  — 6 tests  ✓
LogQueryServiceTest   — 3 tests  ✓
Tests run: 9, Failures: 0, Errors: 0
```

---

## Author

**Harsh Rastogi** — [github.com/harshrastogi15](https://github.com/harshrastogi15)
