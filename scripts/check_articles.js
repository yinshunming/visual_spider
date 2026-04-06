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

    const articles = await client.query('SELECT id, url, title, author, source, publish_date FROM article ORDER BY id DESC LIMIT 10');
    console.log('=== Articles ===');
    console.log(JSON.stringify(articles.rows, null, 2));

    const articleCount = await client.query('SELECT COUNT(*) as cnt FROM article');
    console.log('\nTotal articles:', articleCount.rows[0].cnt);
  } catch (e) {
    console.error('Error:', e.message);
  } finally {
    await client.end();
  }
}

main();