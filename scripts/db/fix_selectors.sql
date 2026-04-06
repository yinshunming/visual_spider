-- Restore corrupted field_rule selectors for Task 1 and Task 3

-- Task 1: 新浪新闻采集 (id=2,3,4,5,6)
UPDATE field_rule SET selectors = '[{"selector":"h1.main-title","selectorType":"css"}]' WHERE id = 2;  -- title
UPDATE field_rule SET selectors = '[{"selector":"#article_content","selectorType":"css"}]' WHERE id = 3;  -- content
UPDATE field_rule SET selectors = '[{"selector":"span.author","selectorType":"css"}]' WHERE id = 4;  -- author
UPDATE field_rule SET selectors = '[{"selector":"div.date-source span.date","selectorType":"css"}]' WHERE id = 5;  -- publish_date
UPDATE field_rule SET selectors = '[{"selector":".source","selectorType":"css"}]' WHERE id = 6;  -- source

-- Task 3: 新浪NBA新闻采集 (id=14,15,16,17)
UPDATE field_rule SET selectors = '[{"selector":"h1.main-title","selectorType":"css"}]' WHERE id = 14;  -- title
UPDATE field_rule SET selectors = '[{"selector":"#article_content p","selectorType":"css"}]' WHERE id = 15;  -- content (FIXED!)
UPDATE field_rule SET selectors = '[{"selector":"span.author","selectorType":"css"}]' WHERE id = 16;  -- author
UPDATE field_rule SET selectors = '[{"selector":"div.date-source span.date","selectorType":"css"}]' WHERE id = 17;  -- publish_date
