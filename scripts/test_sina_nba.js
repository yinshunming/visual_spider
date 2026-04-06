const { chromium } = require('playwright');

(async () => {
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    const errors = [];
    page.on('console', msg => {
        if (msg.type() === 'error') {
            errors.push(msg.text());
        }
    });

    const testUrl = 'https://sports.sina.com.cn/nba/';
    console.log('=== Testing Sina NBA Page ===');
    console.log('URL:', testUrl);

    try {
        await page.goto(testUrl, { timeout: 60000, waitUntil: 'networkidle' });

        // Wait for content
        await page.waitForTimeout(3000);

        const title = await page.title();
        console.log('Page title:', title);

        // Get all links
        const links = await page.locator('a[href]').all();
        console.log('Total links found:', links.length);

        // Check for NBA detail URLs
        const pattern = /https:\/\/sports\.sina\.com\.cn\/basketball\/nba\/[0-9]{4}-[0-9]{2}-[0-9]{2}\/doc-.*\.shtml/;
        let matchCount = 0;
        let sampleUrls = [];
        for (const link of links) {
            const href = await link.getAttribute('href');
            if (href && pattern.test(href)) {
                matchCount++;
                if (sampleUrls.length < 5) sampleUrls.push(href);
            }
        }
        console.log('URLs matching pattern:', matchCount);
        console.log('Sample URLs:', sampleUrls);

        // Now test navigating to a detail page
        if (sampleUrls.length > 0) {
            console.log('\n=== Testing Detail Page ===');
            const detailPage = await context.newPage();
            try {
                await detailPage.goto(sampleUrls[0], { timeout: 60000, waitUntil: 'networkidle' });
                await detailPage.waitForTimeout(3000);

                const detailTitle = await detailPage.title();
                console.log('Detail page title:', detailTitle);

                // Test selectors
                const titleEl = await detailPage.locator('h1.main-title').count();
                console.log('h1.main-title count:', titleEl);
                if (titleEl > 0) {
                    const titleText = await detailPage.locator('h1.main-title').first().textContent();
                    console.log('Title text:', titleText);
                }

                const contentEl = await detailPage.locator('#article_content').count();
                console.log('#article_content count:', contentEl);

                const contentPEle = await detailPage.locator('#article_content p').count();
                console.log('#article_content p count:', contentPEle);
                if (contentPEle > 0) {
                    const contentText = await detailPage.locator('#article_content p').first().textContent();
                    console.log('Content text (first p):', contentText?.substring(0, 100));
                }

                const authorEl = await detailPage.locator('span.author').count();
                console.log('span.author count:', authorEl);
                if (authorEl > 0) {
                    const authorText = await detailPage.locator('span.author').first().textContent();
                    console.log('Author text:', authorText);
                }

            } catch (e) {
                console.log('Detail page error:', e.message);
            } finally {
                await detailPage.close();
            }
        }

        console.log('\nConsole errors:', errors.length);
        errors.forEach(e => console.log('  ERROR:', e));

    } catch (e) {
        console.log('Page error:', e.message);
    }

    await browser.close();
})();
