CREATE EXTENSION IF NOT EXISTS vector;

-- Optional: if you want Spring AI to manage its own default table, you can skip this.
-- If you prefer a custom table for chunks (nice for control), you can start with:
CREATE TABLE IF NOT EXISTS doc_chunks (
                                          id BIGSERIAL PRIMARY KEY,
                                          document_id BIGINT,
                                          file_name TEXT,
                                          page_number INT,
                                          content TEXT NOT NULL,
                                          embedding vector(1536)  -- set to your embedding dim
    );

CREATE INDEX IF NOT EXISTS doc_chunks_embedding_idx ON doc_chunks
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);
