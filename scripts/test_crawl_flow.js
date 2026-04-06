const { chromium } = require('playwright');

(async () => {
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    // Collect console errors
    const errors = [];
    page.on('console', msg => {
        if (msg.type() === 'error') errors.push(msg.text());
    });

    const seedUrl = 'https://sports.sina.com.cn/nba/';
    const detailUrlPattern = 'https://sports\\.sina\\.com\\.cn/basketball/nba/[0-9]{4}-[0-9]{2}-[0-9]{2}/doc-.*\\.shtml';

    console.log('=== Simulating Java CrawlExecutionService ===\n');

    try {
        // Step 1: Navigate to seed URL (like Java code)
        console.log('1. Navigating to seed URL...');
        await page.goto(seedUrl, { timeout: 60000 });
        await page.waitForLoadState('load');
        console.log('   page.url():', page.url());
        console.log('   title:', await page.title());

        // Step 2: Extract URLs (like extractDetailUrls)
        console.log('\n2. Extracting detail URLs...');
        const links = await page.locator('a[href]').all();
        console.log('   Total links:', links.length);

        const regex = new RegExp(detailUrlPattern);
        let matchedUrls = [];
        for (const link of links) {
            const href = await link.getAttribute('href');
            if (href) {
                const fullUrl = new URL(href, page.url()).href;
                if (regex.test(fullUrl)) {
                    matchedUrls.push(fullUrl);
                }
            }
        }
        console.log('   Matched URLs:', matchedUrls.length);
        console.log('   First 5:', matchedUrls.slice(0, 5));

        // Step 3: Navigate to first matched URL
        if (matchedUrls.length > 0) {
            const firstUrl = matchedUrls[0];
            console.log('\n3. Navigating to first detail URL...');
            console.log('   URL:', firstUrl);

            const detailPage = await context.newPage();
            try {
                await detailPage.goto(firstUrl, { timeout: 60000 });
                await detailPage.waitForLoadState('load');
                // Wait extra for JS rendering
                await detailPage.waitForTimeout(5000);
                console.log('   Detail page title:', await detailPage.title());
                console.log('   Detail page.url():', detailPage.url());

                // Step 4: Extract fields (like extractArticle)
                console.log('\n4. Extracting fields...');

                // title
                const titleCount = await detailPage.locator('h1.main-title').count();
                console.log('   h1.main-title count:', titleCount);
                let titleText = null;
                if (titleCount > 0) {
                    titleText = await detailPage.locator('h1.main-title').first().textContent();
                    console.log('   title text:', titleText?.substring(0, 50));
                }

                // content
                const contentCount = await detailPage.locator('#article_content p').count();
                console.log('   #article_content p count:', contentCount);
                let contentText = null;
                if (contentCount > 0) {
                    const allP = await detailPage.locator('#article_content p').allTextContents();
                    contentText = allP.join('\n');
                    console.log('   content length:', contentText.length);
                    console.log('   content preview:', contentText?.substring(0, 80));
                }

                // author
                const authorCount = await detailPage.locator('span.author').count();
                console.log('   span.author count:', authorCount);
                let authorText = null;
                if (authorCount > 0) {
                    authorText = await detailPage.locator('span.author').first().textContent();
                    console.log('   author text:', authorText);
                }

                // publish_date
                const dateCount = await detailPage.locator('div.date-source span.date').count();
                console.log('   div.date-source span.date count:', dateCount);
                let dateText = null;
                if (dateCount > 0) {
                    dateText = await detailPage.locator('div.date-source span.date').first().textContent();
                    console.log('   date text:', dateText);
                }

                // hasContent check
                const hasContent = (titleText && titleText.trim()) || (contentText && contentText.trim());
                console.log('\n5. hasContent:', hasContent);

                // Now check Java regex .find() behavior
                console.log('\n6. Java regex .find() behavior test:');
                const testUrls = [
                    'https://sports.sina.com.cn/basketball/nba/2026-04-05/doc-inhtmsux1965507.shtml',
                    'https://sports.sina.com.cn/g/nba/2026-04-05/doc-inhtmsux1965507.shtml',
                ];
                for (const testUrl of testUrls) {
                    const m = regex.exec(testUrl);
                    console.log('   regex.exec("' + testUrl + '"):', m ? 'MATCH' : 'NO MATCH');
                }

            } catch (e) {
                console.log('   Error:', e.message);
            } finally {
                await detailPage.close();
            }
        }

        console.log('\nConsole errors:', errors.length);
        errors.forEach(e => console.log('  ERROR:', e));

    } catch (e) {
        console.log('Error:', e.message);
    }

    await browser.close();
})();
