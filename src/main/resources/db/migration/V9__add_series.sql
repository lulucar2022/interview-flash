CREATE TABLE IF NOT EXISTS series (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    cover_image VARCHAR(500),
    article_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_series_user_id ON series(user_id);

ALTER TABLE articles ADD COLUMN IF NOT EXISTS series_id BIGINT REFERENCES series(id);
ALTER TABLE articles ADD COLUMN IF NOT EXISTS series_order INTEGER;
CREATE INDEX IF NOT EXISTS idx_articles_series_id ON articles(series_id);
