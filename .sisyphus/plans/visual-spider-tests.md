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
- [x] P1: GET `/` → 200
- [x] P2: GET `/tasks` → 200
- [x] P3: GET `/tasks/new` → 200
- [x] P4: GET `/tasks/1` → 200（任务存在）
- [x] P5: GET `/tasks/99999` → 302 重定向
- [x] P6: GET `/tasks/1/run` → 302 → `/editor/1`
- [x] P7: GET `/articles` → 200
- [x] P8: GET `/sessions` → 200
- [x] P9: GET `/sessions/5` → 200（会话存在）
- [x] P10: GET `/sessions/99999` → 302 重定向
- [x] P11: GET `/sessions/detail?sessionId=5` → 200
- [x] P12: GET `/editor` → 200
- [x] P13: GET `/editor/1` → 200
- [x] P14: GET `/sessions/files/{sessionId}/{filename}` → 200/404

### REST API — CrawlTaskController
- [x] T1: GET `/api/tasks` → 200，JSON 数组
- [x] T2: GET `/api/tasks/1` → 200
- [x] T3: GET `/api/tasks/99999` → 404
- [x] T4: POST `/api/tasks`（最小参数）→ 201，返回 ID
- [x] T5: POST `/api/tasks`（完整参数）→ 201，字段正确
- [x] T6: PUT `/api/tasks/1` → 200
- [x] T7: PUT `/api/tasks/99999` → 404
- [x] T8: DELETE `/api/tasks/1` → 200

### REST API — CrawlSessionController
- [x] S1: GET `/api/sessions` → 200
- [x] S2: GET `/api/sessions/5` → 200
- [x] S3: GET `/api/sessions/99999` → 404
- [x] S4: GET `/api/sessions/task/1` → 200

### REST API — CrawlController
- [x] C1: POST `/api/crawl/start/99999` → 400（无效任务）
- [x] C2: GET `/api/crawl/schedules/99999/last-run` → 404
- [x] C3: GET `/api/crawl/schedules` → 200

### REST API — FieldRuleController
- [x] F1: GET `/api/field-rules` → 200
- [x] F2: GET `/api/field-rules?taskId=1` → 200
- [x] F3: POST `/api/field-rules/batch` → 200
- [x] F4: DELETE `/api/field-rules/1` → 200

### REST API — ArticleController
- [x] A1: GET `/api/articles` → 200
- [x] A2: GET `/api/articles/1` → 200
- [x] A3: POST `/api/articles` → 201
- [x] A4: PUT `/api/articles/1` → 200
- [x] A5: DELETE `/api/articles/1` → 200

### REST API — PageSnapshotController
- [x] N1: GET `/api/snapshots` → 200
- [x] N2: GET `/api/snapshots/1` → 200
- [x] N3: GET `/api/snapshots/session/5` → 200
- [x] N4: POST `/api/snapshots` → 201
- [x] N5: DELETE `/api/snapshots/1` → 200

### 端到端流程
- [x] X1: 新建任务 → 查看详情 → 触发爬取 → 查看会话列表 → 查看会话详情（Mock 爬取逻辑）
- [x] X2: 新建任务（表单页）→ 填写 → 提交 → 跳转详情页
- [x] X3: 字段规则 批量创建 → 查询列表 → 按任务过滤 → 删除

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
- [x] 添加 H2 依赖到 pom.xml（test scope）
- [x] 创建测试配置文件 `src/test/resources/application-test.yml`
- [x] 验证 H2 内存数据库可正常初始化 Schema
- [x] 创建 BaseControllerTest 抽象类（MockMvc setup）

### M2: PageController 测试（P1–P14）
- [x] 编写 PageControllerTest.java
- [x] 使用 @MockBean 模拟 Mapper 层
- [x] 覆盖所有 14 个路由

### M3: REST API Controller 测试（T/S/C/F/A/N）
- [x] CrawlTaskControllerTest（T1–T8）
- [x] CrawlSessionControllerTest（S1–S4）
- [x] CrawlControllerTest（C1–C3）
- [x] FieldRuleControllerTest（F1–F4）
- [x] ArticleControllerTest（A1–A5）
- [x] PageSnapshotControllerTest（N1–N5）

### M4: 端到端流程测试（X1–X3）
- [x] E2E_flowTest.java

### M5: 验证
- [x] `mvn test` 全部通过
- [ ] 覆盖率报告（可选：Jacoco）

### M6: Service 层单元测试
- [x] FieldRuleServiceTest — 23 tests（V1–V23，validate/saveBatch/listAll/listByTaskId/deleteById）
- [x] CrawlSchedulerServiceTest — 14 tests（S1–S13，register/remove/tryStart/onComplete/getSchedule/getAllSchedules）
- [x] SnapshotServiceTest — 5 tests（N1–N5，正常流程 + 异常路径）

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
