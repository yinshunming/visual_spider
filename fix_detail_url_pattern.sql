-- Fix detail_url_pattern: remove double escaping
-- In Java regex, \. matches a literal dot
-- The DB should store \. not \\.
UPDATE crawl_task SET detail_url_pattern = 'https://sports\.sina\.com\.cn/basketball/nba/[0-9]{4}-[0-9]{2}-[0-9]{2}/doc-.*\.shtml' WHERE id = 3;
