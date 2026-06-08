CREATE TABLE IF NOT EXISTS article_daily_views (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    count INTEGER DEFAULT 0,
    UNIQUE (user_id, date)
);

CREATE INDEX IF NOT EXISTS idx_adv_user_date ON article_daily_views(user_id, date);
