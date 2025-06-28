  -- 删除旧的表（如果存在）
  DROP TABLE IF EXISTS public.vector_store;

  -- 创建新的表，使用UUID作为主键
  CREATE TABLE public.vector_store (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      content TEXT NOT NULL,
      metadata JSONB,
     embedding VECTOR(1536)
  );
