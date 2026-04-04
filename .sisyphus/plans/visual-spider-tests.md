# Visual Spider 测试计划

## 概述

为 visual_spider 项目编写完整的测试用例，覆盖所有页面路由、REST API 和核心功能流程。

**技术栈**：JUnit 5 + Spring Boot Test (MockMvc) + MyBatis + H2 内存数据库（测试隔离）

**约束**：
- 使用 H2 内存数据库，测试结束后自动清理
- 不依赖外部 PostgreSQL
- 不启动真实 Playwright 浏览器（SelectorController 相关测试用 Mock）
- 测试类放在 `backend/src/test/java/com/visualspider/` 下，对应各自的模块包

---

## 测试范围

### 页面路由（PageController）
- [ ] P1: GET `/` → 200
- [ ] P2: GET `/tasks` → 200
- [ ] P3: GET `/tasks/new` → 200
- [ ] P4: GET `/tasks/1` → 200（任务存在）
- [ ] P5: GET `/tasks/99999` → 302 重定向
- [ ] P6: GET `/tasks/1/run` → 302 → `/editor/1`
- [ ] P7: GET `/articles` → 200
- [ ] P8: GET `/sessions` → 200
- [ ] P9: GET `/sessions/5` → 200（会话存在）
- [ ] P10: GET `/sessions/99999` → 302 重定向
- [ ] P11: GET `/sessions/detail?sessionId=5` → 200
- [ ] P12: GET `/editor` → 200
- [ ] P13: GET `/editor/1` → 200
- [ ] P14: GET `/sessions/files/{sessionId}/{filename}` → 200/404

### REST API — CrawlTaskController
- [ ] T1: GET `/api/tasks` → 200，JSON 数组
- [ ] T2: GET `/api/tasks/1` → 200
- [ ] T3: GET `/api/tasks/99999` → 404
- [ ] T4: POST `/api/tasks`（最小参数）→ 201，返回 ID
- [ ] T5: POST `/api/tasks`（完整参数）→ 201，字段正确
- [ ] T6: PUT `/api/tasks/1` → 200
- [ ] T7: PUT `/api/tasks/99999` → 404
- [ ] T8: DELETE `/api/tasks/1` → 200

### REST API — CrawlSessionController
- [ ] S1: GET `/api/sessions` → 200
- [ ] S2: GET `/api/sessions/5` → 200
- [ ] S3: GET `/api/sessions/99999` → 404
- [ ] S4: GET `/api/sessions/task/1` → 200

### REST API — CrawlController
- [ ] C1: POST `/api/crawl/start/99999` → 400（无效任务）
- [ ] C2: GET `/api/crawl/schedules/99999/last-run` → 404
- [ ] C3: GET `/api/crawl/schedules` → 200

### REST API — FieldRuleController
- [ ] F1: GET `/api/field-rules` → 200
- [ ] F2: GET `/api/field-rules?taskId=1` → 200
- [ ] F3: POST `/api/field-rules/batch` → 200
- [ ] F4: DELETE `/api/field-rules/1` → 200

### REST API — ArticleController
- [ ] A1: GET `/api/articles` → 200
- [ ] A2: GET `/api/articles/1` → 200
- [ ] A3: POST `/api/articles` → 201
- [ ] A4: PUT `/api/articles/1` → 200
- [ ] A5: DELETE `/api/articles/1` → 200

### REST API — PageSnapshotController
- [ ] N1: GET `/api/snapshots` → 200
- [ ] N2: GET `/api/snapshots/1` → 200
- [ ] N3: GET `/api/snapshots/session/5` → 200
- [ ] N4: POST `/api/snapshots` → 201
- [ ] N5: DELETE `/api/snapshots/1` → 200

### 端到端流程
- [ ] X1: 新建任务 → 查看详情 → 触发爬取 → 查看会话列表 → 查看会话详情（Mock 爬取逻辑）
- [ ] X2: 新建任务（表单页）→ 填写 → 提交 → 跳转详情页
- [ ] X3: 字段规则 批量创建 → 查询列表 → 按任务过滤 → 删除

---

## 测试文件结构

```
backend/src/test/java/com/visualspider/
├── controller/
│   ├── PageControllerTest.java        # P1–P14
│   ├── CrawlTaskControllerTest.java   # T1–T8
│   ├── CrawlSessionControllerTest.java # S1–S4
│   ├── CrawlControllerTest.java       # C1–C3
│   ├── FieldRuleControllerTest.java   # F1–F4
│   ├── ArticleControllerTest.java      # A1–A5
│   └── PageSnapshotControllerTest.java # N1–N5
└── integration/
    └── E2E_flowTest.java              # X1–X3
```

---

## 里程碑

### M1: 测试基础设施
- [ ] 添加 H2 依赖到 pom.xml（test scope）
- [ ] 创建测试配置文件 `src/test/resources/application-test.yml`
- [ ] 验证 H2 内存数据库可正常初始化 Schema
- [ ] 创建 BaseControllerTest 抽象类（MockMvc setup）

### M2: PageController 测试（P1–P14）
- [ ] 编写 PageControllerTest.java
- [ ] 使用 @MockBean 模拟 Mapper 层
- [ ] 覆盖所有 14 个路由

### M3: REST API Controller 测试（T/S/C/F/A/N）
- [ ] CrawlTaskControllerTest（T1–T8）
- [ ] CrawlSessionControllerTest（S1–S4）
- [ ] CrawlControllerTest（C1–C3）
- [ ] FieldRuleControllerTest（F1–F4）
- [ ] ArticleControllerTest（A1–A5）
- [ ] PageSnapshotControllerTest（N1–N5）

### M4: 端到端流程测试（X1–X3）
- [ ] E2E_flowTest.java

### M5: 验证
- [ ] `mvn test` 全部通过
- [ ] 覆盖率报告（可选：Jacoco）

---

## 技术细节

### H2 配置
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
  sql:
    init:
      mode: always
      schema-locations: classpath:schema-test.sql
```

### Schema 初始化
- 测试使用与主库相同的 Schema（H2 兼容模式）
- 每个测试方法结束后自动回滚（@Transactional）

### MockMvc 设置（BaseControllerTest）
```java
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public abstract class BaseControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
```

### Mapper Mock
- 使用 `@MockBean` 模拟所有 Mapper
- 在 @BeforeEach 中配置 Mock 行为（when...thenReturn）
- 避免真实 DB 依赖，保证测试稳定性
