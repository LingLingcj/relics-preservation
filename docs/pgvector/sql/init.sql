CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
   id uuid default gen_random_uuid() PRIMARY KEY,
   content text,
   metadata jsonb,
   embedding vector(4096)
);

