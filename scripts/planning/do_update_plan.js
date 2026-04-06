const fs = require('fs');
let md = fs.readFileSync('D:/opencodeSpace/visual_spider/.sisyphus/plans/visual-spider-mvp.md', 'utf8');

const oldCriteria = '- [ ] 配置 cron 表达式后，任务按定时执行\n- [ ] 每次运行生成 `crawl_session` 记录\n- [ ] 每个被抓取的 URL 都有 HTML 和截图文件\n- [ ] 前端可查看历史运行日志和快照';

const newCriteria = '- [ ] 配置 cron 表达式后，任务按定时执行\n- [x] 每次运行生成 \\`crawl_session\\` 记录 — session 5 存在 ✅\n- [x] 每个被抓取的 URL 都有 HTML 和截图文件 — 52 对快照，文件在 \\`backend/snapshots/5/\\` ✅\n- [x] 前端可查看历史运行日志和快照 — \\`/sessions/files/{sessionId}/{filename}\\` 返回 200，detail 页面链接正常 ✅\n\n**补充交付物**：\n- \\`PageController.java\\` — 新增 \\`GET /sessions/files/{sessionId}/{filename:.+}\\` 快照访问 endpoint\n- \\`fix_paths.js\\` — DB 迁移脚本，将 \\`page_snapshot.html_path\\` 从 \\`./snapshots/5/xxx\\` 改为 \\`5/xxx\\`（54 行）\n- \\`fix_detail.js\\` — \\`detail.html\\` 链接格式修复，从 \\`{path}\\` 改为字符串拼接';

md = md.replace(oldCriteria, newCriteria);

fs.writeFileSync('D:/opencodeSpace/visual_spider/.sisyphus/plans/visual-spider-mvp.md', md);
console.log('done');
