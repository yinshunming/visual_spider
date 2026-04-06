const fs = require('fs');
const path = 'D:/opencodeSpace/visual_spider/backend/src/main/resources/templates/sessions/detail.html';
let content = fs.readFileSync(path, 'utf8');

// Replace the two links
content = content.replace(
  '@{/sessions/files/{path}(path=${snapshot.htmlPath})}',
  "@{'/sessions/files/' + ${snapshot.htmlPath}}"
);
content = content.replace(
  '@{/sessions/files/{path}(path=${snapshot.screenshotPath})}',
  "@{'/sessions/files/' + ${snapshot.screenshotPath}}"
);

fs.writeFileSync(path, content, 'utf8');
console.log('Updated detail.html');
