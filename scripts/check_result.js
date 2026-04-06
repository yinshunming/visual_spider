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

    const sessions = await client.query('SELECT id, task_id, status, pages_crawled, articles_extracted, error_message FROM crawl_session ORDER BY id DESC LIMIT 5');
    console.log('=== Recent Sessions ===');
    console.log(JSON.stringify(sessions.rows, null, 2));

    const articles = await client.query('SELECT id, task_id, url, title, author FROM article ORDER BY id DESC LIMIT 10');
    console.log('\n=== Recent Articles ===');
    console.log(JSON.stringify(articles.rows, null, 2));

    const articleCount = await client.query('SELECT COUNT(*) as cnt FROM article');
    console.log('\n=== Total Articles ===');
    console.log(articleCount.rows[0]);
  } catch (e) {
    console.error('Error:', e.message);
  } finally {
    await client.end();
  }
}

main();