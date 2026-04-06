const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  const url = 'https://sports.sina.com.cn/basketball/nba/2026-04-05/doc-inhtmsux1965507.shtml';
  await page.goto(url, { timeout: 60000, waitUntil: 'load' });
  
  // 尝试各种内容选择器
  const contentSelectors = [
    '#article_content',
    '#article_content .article-content-left',
    '#article_content .article-content-left p',
    '.article-content',
    '.article-content-left',
    '.article-content-left p',
    '#article_content p',
    '.article-content p',
    '.txt',
    '.article',
    '.article-body',
    '.article-content',
    '.article-text',
  ];
  
  console.log('=== Content selector tests ===');
  for (const sel of contentSelectors) {
    const loc = page.locator(sel);
    const count = await loc.count();
    if (count > 0) {
      const text = await loc.first().textContent();
      console.log(`[${sel}] count=${count} text_len=${text ? text.trim().length : 0} preview='${text ? text.trim().substring(0, 80).replace(/\n/g, ' ') : 'null'}'`);
    } else {
      console.log(`[${sel}] count=0`);
    }
  }
  
  // 直接获取所有 p 标签内容
  console.log('\n=== All <p> tags in article_content ===');
  const pTags = await page.locator('#article_content p').all();
  console.log(`Total <p> tags: ${pTags.length}`);
  let totalText = '';
  for (const p of pTags) {
    const t = (await p.textContent()).trim();
    if (t.length > 20) {
      totalText += t + '\n';
    }
  }
  console.log(`Total article text from <p>: ${totalText.length} chars`);
  console.log(`Preview: ${totalText.substring(0, 200)}`);
  
  await browser.close();
})();
