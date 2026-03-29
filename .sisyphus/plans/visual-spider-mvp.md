# Visual Spider MVP - 可视化爬虫

## 1. 概述与目标

基于 Java 的可视化爬虫 MVP，允许用户通过点击页面元素来配置抓取规则，无需编写代码。

**技术栈**：Java 21 + Spring Boot + Thymeleaf + MyBatis + PostgreSQL + Quartz + Playwright for Java

**约束**：
- 单体应用
- 不做分布式爬虫
- 不使用 iframe 嵌入第三方页面

---

## 2. 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Thymeleaf)                     │
│  URL输入 → 页面渲染预览 → 元素点击配置规则 → 字段映射 → 结果预览  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                     Spring Boot 应用                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────────┐ │
│  │  控制器层  │  │  服务层   │  │  定时器   │  │  Playwright │ │
│  │ Controller│  │ Service  │  │ Quartz   │  │   Client    │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 数据库模型

### 3.1 article（目标表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| url | VARCHAR(2048) | 文章 URL |
| title | VARCHAR(500) | 标题 |
| content | TEXT | 正文（纯文本） |
| author | VARCHAR(255) | 作者 |
| publish_date | DATE | 发布日期 |
| source | VARCHAR(255) | 来源网站名 |
| created_at | TIMESTAMP | 抓取时间 |
| updated_at | TIMESTAMP | 更新时间 |
| status | VARCHAR(50) | 状态 |

### 3.2 field_rule（字段规则）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| field_name | VARCHAR(100) | 预定义字段名 |
| selector | TEXT | 选择器 |
| selector_type | VARCHAR(50) | CSS / XPATH |
| extraction_type | VARCHAR(50) | text / attribute / html |
| attribute_name | VARCHAR(255) | 属性名（当 extraction_type=attribute 时） |
| task_id | BIGINT | 关联任务 |
| created_at | TIMESTAMP | 创建时间 |

### 3.3 crawl_task（抓取任务）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| name | VARCHAR(255) | 任务名 |
| seed_url | VARCHAR(2048) | 起始 URL |
| pagination_selector | TEXT | 分页选择器 |
| pagination_type | VARCHAR(50) | button / load_more |
| detail_url_pattern | VARCHAR(500) | 详情页 URL 正则 |
| max_pages | INTEGER | 最大翻页数 |
| enabled | BOOLEAN | 是否启用 |
| cron_expression | VARCHAR(100) | cron 表达式 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 3.4 crawl_session（运行日志）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| task_id | BIGINT | 关联任务 |
| start_time | TIMESTAMP | 开始时间 |
| end_time | TIMESTAMP | 结束时间 |
| status | VARCHAR(50) | RUNNING / COMPLETED / FAILED |
| pages_crawled | INTEGER | 抓取页面数 |
| articles_extracted | INTEGER | 提取 article 数 |
| error_message | TEXT | 错误信息 |

### 3.5 page_snapshot（页面快照）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| session_id | BIGINT | 关联 session |
| url | VARCHAR(2048) | 页面 URL |
| html_path | VARCHAR(500) | HTML 文件路径 |
| screenshot_path | VARCHAR(500) | 截图文件路径 |
| created_at | TIMESTAMP | 创建时间 |

---

## 4. 里程碑

### M1: 项目骨架 + 数据模型 + 基础 CRUD

**目标**：搭建可运行的项目骨架，完成数据库表设计和基础 CRUD。

**范围**：
- [ ] Spring Boot 项目初始化（JDK-21, Maven）
- [ ] application.yml 配置（PostgreSQL + Quartz）
- [ ] MyBatis 集成（注解方式）
- [ ] 5 张表的实体类创建
- [ ] 5 张表的 Mapper 接口和 CRUD 方法
- [ ] REST API Controller（article / field_rule / crawl_task / crawl_session / page_snapshot）
- [ ] Thymeleaf 基础页面骨架（首页）

**验收标准**：
- `./mvnw clean compile` 通过
- 5 张表的增删改查 REST API 可正常调用
- Thymeleaf 首页可访问（无需功能）
- 无 `@Playwright` 相关代码（尚未引入）

---

### M2: Playwright 集成 + URL 渲染 + 元素点击

**目标**：在浏览器中打开真实 URL，点击页面元素生成 5 个候选 selector。

**范围**：
- [ ] Playwright for Java 依赖引入
- [ ] Playwright 配置类（独立浏览器进程模式）
- [ ] PlaywrightService 服务类（页面加载、元素点击拦截）
- [ ] 元素信息收集（tag, class, id, attributes, text content, parent chain）
- [ ] 5 个候选 selector 生成逻辑
- [ ] 弹窗 UI（显示 5 个候选 + 提取方式选择）

**验收标准**：
- 输入 `https://news.ycombinator.com` 能渲染页面
- 点击任意元素，弹出 5 个候选 selector
- 选择一个 selector + 提取方式后，提取到对应文本/属性
- `CrawlSession` 记录每次操作的 URL 和时间

---

### M3: 字段映射 + 预览 + 校验

**目标**：将规则映射到 article 表，预览抽取结果并校验。

**范围**：
- [ ] 预定义字段枚举（title, content, author, publish_date, source）
- [ ] 字段映射配置 UI
- [ ] 预览模式（输入 URL，用当前规则提取 → 显示 article 预览）
- [ ] 字段校验规则实现
- [ ] 规则保存到数据库

**验收标准**：
- 点击"预览"按钮，对给定 URL 显示 article 预览
- 必填字段为空时显示红色警告
- 规则保存后可在列表页查看

---

### M4: 递归翻页抓取

**目标**：支持列表页自动翻页 → 进入详情页提取。

**范围**：
- [ ] CrawlTask 配置扩展（pagination_selector / pagination_type / max_pages / detail_url_pattern）
- [ ] 列表页链接提取
- [ ] 翻页逻辑（点击下一页 → 重复抓取）
- [ ] 详情页 article 提取
- [ ] URL 去重机制

**验收标准**：
- 配置好规则后，运行任务能从列表页自动翻页抓取多篇 article
- 每篇 article 的字段正确提取
- 已抓取 URL 不重复抓取

---

### M5: Quartz 定时执行 + 快照 + 审计日志

**目标**：定时调度任务，保存快照和运行日志。

**范围**：
- [ ] Quartz 集成
- [ ] 定时调度逻辑
- [ ] 快照策略（HTML + 截图）
- [ ] 快照存储（文件系统 + DB 记录路径）
- [ ] 运行日志记录
- [ ] 前端日志查看和快照展示

**验收标准**：
- 配置 cron 表达式后，任务按定时执行
- 每次运行生成 `crawl_session` 记录
- 每个被抓取的 URL 都有 HTML 和截图文件
- 前端可查看历史运行日志和快照

---

### M6: MVP 端到端验证

**目标**：完整流程可运行，无阻塞性 bug。

**范围**：
- [ ] 端到端测试用例
- [ ] 异常处理（超时重试、selector 匹配失败）
- [ ] 页面优化（加载中、错误提示、结果分页）

**验收标准**：
- `./mvnw clean package -DskipTests` 通过
- 能抓取至少一个真实网站（自测通过）
- 所有 API 端点返回正确状态码
- 无 console error

---

## 5. 关键技术决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| Playwright 模式 | 独立进程模式 | 避免容器兼容问题 |
| 快照存储 | 文件系统 + DB 记录路径 | 审计追溯，文件量大不存 BLOB |
| 预定义字段 | 5 个固定字段 | title, content, author, publish_date, source |
| 递归终止条件 | max_pages | 防止死循环，默认 10 |
| 重试策略 | 3 次，间隔 5s | MVP 简单重试 |

---

## 6. Final Verification Wave

- [ ] **F1**: 所有里程碑代码可独立编译通过
- [ ] **F2**: 端到端抓取流程验证成功
- [ ] **F3**: 代码符合 Java 规范，无重大设计缺陷
- [ ] **F4**: 文档完整（README + API 文档）
