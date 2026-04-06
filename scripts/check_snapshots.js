const { Client } = require('pg');

async function main() {
  const client = new Client({
    host: 'localhost',
    port: 5432,
    user: 'postgres',
    password: '123456',
    database: 'postgres'
  });

  try {
    await client.connect();

    // Session 5 的快照
    const snapshots = await client.query(
      'SELECT id, session_id, url, html_path, screenshot_path FROM page_snapshot WHERE session_id = 5 ORDER BY id'
    );
    console.log('=== Session 5 Snapshots ===');
    console.log(JSON.stringify(snapshots.rows, null, 2));

    // 检查快照文件是否存在
    const fs = require('fs');
    for (const row of snapshots.rows) {
      const htmlExists = fs.existsSync(row.html_path);
      const screenshotExists = fs.existsSync(row.screenshot_path);
      console.log(`\n[${row.id}] ${row.url}`);
      console.log(`  HTML:  ${row.html_path} (exists: ${htmlExists})`);
      console.log(`  Image: ${row.screenshot_path} (exists: ${screenshotExists})`);
    }
  } catch (e) {
    console.error('Error:', e.message);
  } finally {
    await client.end();
  }
}

main();