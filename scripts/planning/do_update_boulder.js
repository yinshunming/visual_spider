const fs = require('fs');
const data = JSON.parse(fs.readFileSync('D:/opencodeSpace/visual_spider/.sisyphus/boulder.json', 'utf8'));
data.current_milestone = 'M6';
data.completed_milestones = ['M1','M2','M3','M4','M5'];
data.next_milestone = 'M6';
fs.writeFileSync('D:/opencodeSpace/visual_spider/.sisyphus/boulder.json', JSON.stringify(data, null, 2));
console.log('done');
