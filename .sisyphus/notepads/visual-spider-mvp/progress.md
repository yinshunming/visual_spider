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

### 下一步：M2 Playwright 集成 + URL 渲染 + 元素点击

**待实现**：
- Playwright for Java 依赖引入
- Playwright 配置类（独立浏览器进程模式）
- PlaywrightService 服务类（页面加载、元素点击拦截）
- 元素信息收集（tag, class, id, attributes, text content, parent chain）
- 5 个候选 selector 生成逻辑
- 弹窗 UI（显示 5 个候选 + 提取方式选择）

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
