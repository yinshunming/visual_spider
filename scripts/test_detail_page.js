const { chromium } = require('playwright');

(async () => {
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    const testUrl = 'https://sports.sina.com.cn/basketball/nba/2026-04-05/doc-inhtmsux1965507.shtml';
    console.log('=== Testing Detail Page with "load" event ===');
    console.log('URL:', testUrl);

    try {
        // Use 'load' instead of 'networkidle'
        await page.goto(testUrl, { timeout: 60000, waitUntil: 'load' });
        console.log('Page loaded with "load" event');

        // Wait for JS to render content
        await page.waitForTimeout(5000);

        const title = await page.title();
        console.log('Page title:', title);

        // Test selectors WITHOUT waiting for specific elements
        const titleEl = await page.locator('h1.main-title').count();
        console.log('h1.main-title count:', titleEl);
        if (titleEl > 0) {
            const titleText = await page.locator('h1.main-title').first().textContent();
            console.log('Title text:', titleText);
        }

        const contentEl = await page.locator('#article_content').count();
        console.log('#article_content count:', contentEl);

        const contentPEle = await page.locator('#article_content p').count();
        console.log('#article_content p count:', contentPEle);
        if (contentPEle > 0) {
            const contentText = await page.locator('#article_content p').first().textContent();
            console.log('Content text (first p):', contentText?.substring(0, 100));
            const allText = await page.locator('#article_content p').allTextContents();
            console.log('All p text total length:', allText.join('').length);
        }

        const authorEl = await page.locator('span.author').count();
        console.log('span.author count:', authorEl);
        if (authorEl > 0) {
            const authorText = await page.locator('span.author').first().textContent();
            console.log('Author text:', authorText);
        }

        const dateEl = await page.locator('div.date-source span.date').count();
        console.log('div.date-source span.date count:', dateEl);
        if (dateEl > 0) {
            const dateText = await page.locator('div.date-source span.date').first().textContent();
            console.log('Date text:', dateText);
        }

    } catch (e) {
        console.log('Page error:', e.message);
    }

    await browser.close();
})();
