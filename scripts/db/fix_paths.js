const { Client } = require('pg');
const client = new Client({ host: 'localhost', port: 5432, database: 'postgres', user: 'postgres', password: '123456' });

(async () => {
  await client.connect();
  // 把 htmlPath 和 screenshotPath 从 ./snapshots/5/xxx.html 改为 5/xxx.html
  const res = await client.query(`
    UPDATE page_snapshot 
    SET html_path = REPLACE(html_path, './snapshots/', ''),
        screenshot_path = REPLACE(screenshot_path, './snapshots/', '')
    WHERE html_path LIKE './snapshots/%'
  `);
  console.log('Updated rows:', res.rowCount);
  
  // 验证结果
  const verify = await client.query(`
    SELECT id, html_path, screenshot_path 
    FROM page_snapshot 
    WHERE session_id = 5 
    LIMIT 3
  `);
  console.log('Sample rows:', verify.rows);
  await client.end();
})();
