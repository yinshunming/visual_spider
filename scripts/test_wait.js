// Test if the extractField logic works correctly
const { chromium } = require('playwright');

(async () => {
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    const detailUrl = 'https://sports.sina.com.cn/basketball/nba/2026-04-05/doc-inhtmsux1965507.shtml';
    console.log('Testing detail page extraction...\nURL:', detailUrl);

    try {
        await page.goto(detailUrl, { timeout: 60000 });
        await page.waitForLoadState('load');

        // Test waitForSelector behavior
        console.log('\n1. Testing waitForSelector("h1.main-title"):');
        try {
            await page.waitForSelector('h1.main-title', { timeout: 30000 });
            console.log('   SUCCESS: h1.main-title found immediately');
        } catch (e) {
            console.log('   TIMEOUT: h1.main-title not found within 30s');
        }

        // Test without waitForSelector
        console.log('\n2. Testing locator count WITHOUT waitForSelector:');
        const h1Count = await page.locator('h1.main-title').count();
        console.log('   h1.main-title count:', h1Count);

        const contentPCount = await page.locator('#article_content p').count();
        console.log('   #article_content p count:', contentPCount);

        // Now wait 5s and test again
        console.log('\n3. After 5s wait:');
        await page.waitForTimeout(5000);
        const h1CountAfter = await page.locator('h1.main-title').count();
        console.log('   h1.main-title count:', h1CountAfter);

        const contentPCountAfter = await page.locator('#article_content p').count();
        console.log('   #article_content p count:', contentPCountAfter);

        // Test text extraction
        console.log('\n4. Text extraction test:');
        if (h1CountAfter > 0) {
            const titleText = await page.locator('h1.main-title').first().textContent();
            console.log('   title:', titleText?.substring(0, 50));
            console.log('   isBlank:', !titleText || titleText.trim().length === 0);
        }

        if (contentPCountAfter > 0) {
            const allP = await page.locator('#article_content p').allTextContents();
            const content = allP.join('\n');
            console.log('   content length:', content.length);
            console.log('   content isBlank:', !content || content.trim().length === 0);
        }

    } catch (e) {
        console.log('Error:', e.message);
    }

    await browser.close();
})();
