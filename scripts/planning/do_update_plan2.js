const fs = require('fs');
let md = fs.readFileSync('D:/opencodeSpace/visual_spider/.sisyphus/plans/visual-spider-mvp.md', 'utf8');
md = md.replace(
  '- [ ] 配置 cron 表达式后，任务按定时执行',
  '- [x] 配置 cron 表达式后，任务按定时执行 — Quartz 已集成，CrawlScheduleJob + CrawlSchedulerService 已实现'
);
fs.writeFileSync('D:/opencodeSpace/visual_spider/.sisyphus/plans/visual-spider-mvp.md', md);
console.log('done');
