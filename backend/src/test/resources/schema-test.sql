-- schema-test.sql
-- Combined H2-compatible schema for testing (H2 2.x PostgreSQL mode)

DROP TABLE IF EXISTS field_rule CASCADE;
DROP TABLE IF EXISTS article CASCADE;
DROP TABLE IF EXISTS crawl_task CASCADE;
DROP TABLE IF EXISTS selector_session CASCADE;
DROP TABLE IF EXISTS crawl_session CASCADE;
DROP TABLE IF EXISTS page_snapshot CASCADE;

CREATE TABLE field_rule (
    id BIGINT IDENTITY PRIMARY KEY,
    field_code VARCHAR(100) NOT NULL,
    selectors TEXT NOT NULL,
    extract_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    validations TEXT DEFAULT '[]',
    task_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_field_rule_task_id ON field_rule(task_id);
CREATE INDEX idx_field_rule_field_code ON field_rule(field_code);

CREATE TABLE article (
    id BIGINT IDENTITY PRIMARY KEY,
    url VARCHAR(2000),
    title VARCHAR(500),
    content TEXT,
    author VARCHAR(200),
    publish_date DATE,
    source VARCHAR(100),
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE crawl_task (
    id BIGINT IDENTITY PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    url_pattern VARCHAR(1000),
    status VARCHAR(20) DEFAULT 'active',
    cron_expression VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    seed_url VARCHAR(2048),
    pagination_selector TEXT,
    pagination_type VARCHAR(50),
    detail_url_pattern VARCHAR(500),
    max_pages INTEGER DEFAULT 10,
    enabled BOOLEAN DEFAULT true
);

CREATE TABLE selector_session (
    id VARCHAR(50) PRIMARY KEY,
    url VARCHAR(2000) NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE TABLE crawl_session (
    id BIGINT IDENTITY PRIMARY KEY,
    task_id BIGINT NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(50),
    pages_crawled INTEGER,
    articles_extracted INTEGER,
    error_message TEXT
);

CREATE TABLE page_snapshot (
    id BIGINT IDENTITY PRIMARY KEY,
    session_id BIGINT NOT NULL,
    url VARCHAR(2048),
    html_path VARCHAR(512),
    screenshot_path VARCHAR(512),
    created_at TIMESTAMP
);
