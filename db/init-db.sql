
-- Enable pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- Create table
CREATE TABLE IF NOT EXISTS structured_logs (
    id          BIGSERIAL PRIMARY KEY,
    timestamp   VARCHAR(50),
    level       VARCHAR(10)  NOT NULL,
    service     VARCHAR(100) NOT NULL,
    message     TEXT         NOT NULL,
    raw_log     TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    embedding   vector(384)
);

-- SQL filter indexes
CREATE INDEX IF NOT EXISTS idx_logs_level   ON structured_logs (level);
CREATE INDEX IF NOT EXISTS idx_logs_service ON structured_logs (service);
CREATE INDEX IF NOT EXISTS idx_logs_created ON structured_logs (created_at DESC);

-- HNSW index for vector similarity search
CREATE INDEX IF NOT EXISTS idx_logs_embedding
    ON structured_logs
    USING hnsw (embedding vector_cosine_ops);
