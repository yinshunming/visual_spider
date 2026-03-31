-- V2__fix_crawl_task_columns.sql
-- 修复 crawl_task 表列名不匹配问题
-- V1 使用 url_pattern/status，Entity 使用 seed_url/pagination_selector/pagination_type/detail_url_pattern/max_pages/enabled

-- 添加新列（如果不存在则跳过）
ALTER TABLE crawl_task ADD COLUMN IF NOT EXISTS seed_url VARCHAR(2048);
ALTER TABLE crawl_task ADD COLUMN IF NOT EXISTS pagination_selector TEXT;
ALTER TABLE crawl_task ADD COLUMN IF NOT EXISTS pagination_type VARCHAR(50);
ALTER TABLE crawl_task ADD COLUMN IF NOT EXISTS detail_url_pattern VARCHAR(500);
ALTER TABLE crawl_task ADD COLUMN IF NOT EXISTS max_pages INTEGER DEFAULT 10;
ALTER TABLE crawl_task ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT true;

-- 从 url_pattern 迁移数据到 seed_url（仅当 seed_url 为空时）
UPDATE crawl_task SET seed_url = url_pattern WHERE seed_url IS NULL AND url_pattern IS NOT NULL;

-- 从 status 推断 enabled 值（active=true, 其他=false）
UPDATE crawl_task SET enabled = (status = 'active') WHERE enabled IS NULL;

-- 删除旧列（可选，先注释掉保留数据）
-- ALTER TABLE crawl_task DROP COLUMN IF EXISTS url_pattern;
-- ALTER TABLE crawl_task DROP COLUMN IF EXISTS status;
