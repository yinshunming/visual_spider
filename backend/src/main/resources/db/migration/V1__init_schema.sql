-- V1__init_schema.sql
-- 初始化数据库 schema

-- Drop existing table if needed (for development reset)
DROP TABLE IF EXISTS field_rule CASCADE;

-- Field Rule 表：存储字段提取规则
CREATE TABLE field_rule (
    id BIGSERIAL PRIMARY KEY,
    field_code VARCHAR(100) NOT NULL,           -- 字段代码，如 title, content, author
    selectors TEXT NOT NULL,                    -- 选择器数组 JSON（支持多个候选项）
    extract_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',  -- TEXT / HTML / ATTR
    validations TEXT DEFAULT '[]',              -- 校验规则数组 JSON
    task_id BIGINT,                             -- 关联的抓取任务 ID（可为空，手动关联）
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_field_rule_task_id ON field_rule(task_id);
CREATE INDEX idx_field_rule_field_code ON field_rule(field_code);

-- Article 表（参考目标）
DROP TABLE IF EXISTS article CASCADE;
CREATE TABLE article (
    id BIGSERIAL PRIMARY KEY,
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

-- Crawl Task 表（参考已有）
DROP TABLE IF EXISTS crawl_task CASCADE;
CREATE TABLE crawl_task (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    url_pattern VARCHAR(1000),
    status VARCHAR(20) DEFAULT 'active',
    cron_expression VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Selector Session 表（参考已有）
DROP TABLE IF EXISTS selector_session CASCADE;
CREATE TABLE selector_session (
    id VARCHAR(50) PRIMARY KEY,
    url VARCHAR(2000) NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);
