# Visual Spider MVP - 工作笔记

## 2026-03-28

### 已完成：M1 项目骨架 + 数据模型 + 基础 CRUD

**交付物**：
- Spring Boot 3.2.5 + Java 21 项目骨架
- 5 张表对应实体类（Article, CrawlTask, CrawlSession, FieldRule, PageSnapshot）
- MyBatis Mapper 接口（注解方式）+ CRUD
- REST API Controller × 5
- Thymeleaf 页面（index, tasks, articles, sessions）
- PageController 页面路由

**技术决策**：
- 移除 Lombok（Lombok 1.18.36 与 Java 24 不兼容，报 `TypeTag::UNKNOWN` 错误）
- 实体类使用手写 getter/setter

**遇到的问题**：
1. Lombok + Java 24 不兼容 → 移除 Lombok，改用手写 getter/setter
2. Maven 编译正常通过（仅 guice 的 deprecation warning）

### 下一步：M3 字段映射 + 预览 + 校验

**待实现**：
- 预定义字段枚举（title, content, author, publish_date, source）
- 字段映射配置 UI
- 预览模式（输入 URL，用当前规则提取 → 显示 article 预览）
- 字段校验规则实现
- 规则保存到数据库

---

## 2026-03-29

### 已完成：M2 Playwright 集成 + 元素点击

**简化设计**：
- 浏览器端注入 JS 生成 selector 候选
- 后端仅存储用户选择的 selector（内存，非持久化）
- SSE 实时推送节点信息到监控面板

**交付物**：
- `domain/SelectorSession.java` - 内存会话实体
- `service/SelectorService.java` - 会话管理 + Playwright 浏览器 + SSE
- `controller/SelectorController.java` - REST API（含 SSE 端点）
- `dto/NodeSelection.java`, `SelectorCandidate.java`, `SelectorResult.java`
- `templates/editor.html` - 监控面板

**API 端点**：
- `POST /api/selector/start` - 启动会话，打开浏览器
- `POST /api/selector/session/{id}/select` - 接收节点选择
- `GET /api/selector/session/{id}/events` - SSE 实时推送
- `POST /api/selector/session/{id}/confirm` - 确认选择
- `POST /api/selector/session/{id}/complete` - 完成会话，返回 JSON
- `DELETE /api/selector/session/{id}` - 关闭会话

### 文件清单

```
backend/src/main/java/com/visualspider/
├── VisualSpiderApplication.java
├── controller/
│   ├── ArticleController.java
│   ├── CrawlTaskController.java
│   ├── CrawlSessionController.java
│   ├── FieldRuleController.java
│   ├── PageSnapshotController.java
│   └── PageController.java
├── domain/
│   ├── Article.java
│   ├── CrawlTask.java
│   ├── CrawlSession.java
│   ├── FieldRule.java
│   └── PageSnapshot.java
├── repository/
│   ├── ArticleMapper.java
│   ├── CrawlTaskMapper.java
│   ├── CrawlSessionMapper.java
│   ├── FieldRuleMapper.java
│   └── PageSnapshotMapper.java
backend/src/main/resources/
├── application.yml
├── templates/
│   ├── index.html
│   ├── tasks/index.html
│   ├── articles/index.html
│   └── sessions/index.html
```

### 环境信息
- Java: 24.0.1 (build 24.0.1+9-30)
- Maven: 3.9.11
- Spring Boot: 3.2.5
