-- V3__add_m5_crawl_session_and_page_snapshot.sql
-- M5: Quartz定时执行 + 快照 + 审计日志
-- 创建 crawl_session（抓取会话审计表）和 page_snapshot（页面快照表）

CREATE TABLE crawl_session (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(50),
    pages_crawled INTEGER,
    articles_extracted INTEGER,
    error_message TEXT
);

CREATE TABLE page_snapshot (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    url VARCHAR(2048),
    html_path VARCHAR(512),
    screenshot_path VARCHAR(512),
    created_at TIMESTAMP
);
