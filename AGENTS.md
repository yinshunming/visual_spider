# AGENTS.md - visual_spider 项目规范

> 本文件为 AI 代理提供在 visual_spider 项目中的开发指导。
> 项目技术栈：后端 Spring Boot (JDK-24) + 前端 Vue 3

---

## 0. 开发进度

### 当前阶段：M5 Quartz 定时执行 + 快照 + 审计日志

### 已完成功能

| 模块 | 状态 | 说明 |
|------|------|------|
| M1 可视化选择器 | ✅ | SelectorService、SelectorSession、PageController、editor.html |
| M2 Session 管理 | ✅ | SSE 推送、broadcast、绝对路径注入 |
| M3 字段映射 + 预览 + 校验 + 规则持久化 | ✅ | FieldRule CRUD、previewExtraction、Jackson 序列化 |
| M4 递归翻页抓取执行器 | ✅ | CrawlExecutionService、CrawlController、V2 migration |
| 数据库 Schema | ✅ | `V1__init_schema.sql`，四张表：field_rule / article / crawl_task / selector_session |
| 实体类 | ✅ | `FieldRule`（selectors/validations 存 TEXT/JSON）、`ExtractType` 枚举、`FieldValidation`、`SelectorDef` |
| MyBatis Mapper | ✅ | 注解 SQL，typeHandler 仅用于 SELECT 反序列化 |
| DTO | ✅ | `PreviewRequest`、`PreviewResult`、`NodeSelection`、`FieldRuleRequest`、`FieldRuleResponse` |
| Service | ✅ | `FieldRuleService`（CRUD + JSON 序列化 + 校验逻辑）、`SelectorService`（previewExtraction）、`CrawlExecutionService`（M4） |
| API 接口 | ✅ | `/selector/start`、`/selector/preview`、`/field-rules`（CRUD 全部正常）、`/api/crawl/start/{taskId}` |
| 前端 editor.html | ✅ | 支持选择器类型/extractionType/validations，默认 URL 新浪网 |
| 编译 | ✅ | `mvn compile` 通过 |
| Git 推送 | ✅ | commit `73a8d32` 已推送到 origin/main |

### API 验证状态

| 接口 | 状态 |
|------|------|
| `POST /api/selector/start` | ✅ |
| `POST /api/selector/preview` | ✅ |
| `GET /api/field-rules` | ✅ |
| `POST /api/field-rules/batch` | ✅ |
| `DELETE /api/field-rules/{id}` | ✅ |

### M4 验收标准

- [x] `CrawlExecutionService` 翻页循环 + 详情页提取
- [x] `CrawlController` 手动触发接口
- [x] URL 去重双层机制（内存 Set + 数据库查询）

### M5 待办（Quartz 定时执行 + 快照 + 审计日志）

1. **Quartz 定时任务** — 定时触发爬取
2. **PageSnapshot 快照** — 保存页面快照
3. **审计日志** — 记录爬取历史

### 关键技术细节（需注意）

- **MyBatis 注解限制**：写入时（@Insert/@Update）不能用 inline `typeHandler=`，改为在 Service 层手动 JSON 序列化，Mapper 只做简单 `#{}` 替换
- **PostgreSQL**：Docker 容器 `postgresql`，用户 `postgres`，密码 `123456`，数据库 `postgres`
- **数据库连接**：无 migration 工具，手动执行 `docker exec postgresql psql -U postgres`
- **启动命令**：`mvn spring-boot:run`（无 mvnw），端口 8080
- **Jackson ObjectMapper**：通过 Spring 注入，用于 `FieldRuleService` 的 JSON 序列化/反序列化
- **Session 管理**：`SelectorService` 内存管理 `ConcurrentHashMap`，Playwright Page 对象存在 `SelectorSession.page` 中
- **SSE 推送**：选择结果通过 `SseEmitter` 推送到前端 `/api/selector/sse/{sessionId}`

### Git 历史

```
eedd59f fix: 修复 field-rules batch 500 错误 — 手动 JSON 序列化实现读写分离
0e2fd1f feat: M3 字段映射 + 预览 + 校验 + 规则持久化
98e6ec8 fix: CORS filter 解决跨域预检请求失败
8a793bd fix: M2 selector session broadcast 迭代修复与绝对路径注入
44caf35 feat: M2 简化版 selector session 实现
9a0886c chore: 更新 .gitignore 排除 target/
```

---

## 1. 项目结构

```
visual_spider/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/visualspider/
│   │       ├── config/        # 配置类
│   │       ├── controller/    # REST 控制器
│   │       ├── service/       # 业务逻辑层
│   │       ├── repository/    # 数据访问层
│   │       ├── domain/        # 实体类
│   │       ├── dto/           # 数据传输对象
│   │       └── exception/     # 异常处理
│   ├── src/main/resources/
│   │   └── application.yml   # 配置文件
│   └── src/test/java/        # 测试代码
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── components/       # 组件
│   │   ├── views/           # 页面
│   │   ├── stores/          # 状态管理
│   │   ├── api/             # API 调用
│   │   └── utils/           # 工具函数
│   └── package.json
└── pom.xml                    # Maven 父 POM
```

---

## 2. 构建与测试命令

### 后端 (Spring Boot)

```bash
# 编译打包（项目用 mvn 非 mvnw）
mvn clean package -DskipTests

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=MarketServiceTest

# 运行单个测试方法
mvn test -Dtest=MarketServiceTest#createMarket

# 跳过测试打包
mvn clean package -DskipTests

# 代码检查 (需要配置 checkstyle/spotless)
mvn checkstyle:check
mvn spotless:check

# 格式化代码
mvn spotless:apply
```

### 前端 (Vue)

```bash
# 安装依赖
cd frontend && npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build

# 运行单元测试
npm run test:unit

# 运行单个测试文件
npm run test:unit -- --filter UserService.test.ts

# E2E 测试
npm run test:e2e

# ESLint 检查
npm run lint

# 自动修复 ESLint 问题
npm run lint -- --fix
```

---

## 3. Java 代码规范 (Spring Boot)

### 3.1 命名规范

```java
// ✅ 类名/记录名：PascalCase
public class MarketService {}
public record MarketResponse(Long id, String name) {}

// ✅ 方法名/变量名：camelCase
private MarketRepository marketRepository;
public Market findBySlug(String slug) {}

// ✅ 常量：UPPER_SNAKE_CASE
private static final int MAX_PAGE_SIZE = 100;
```

### 3.2 导入规范

```java
// ✅ 按以下顺序分组，组间空行分隔
import java.*;                    // Java 标准库
import jakarta.*;                 // Jakarta EE
import org.springframework.*;      // Spring 框架
import com.visualspider.*;        // 本项目
import static org.assertj.core.api.Assertions.*;  // 测试断言
```

### 3.3 类型规范

```java
// ✅ 优先使用不可变类型
public record MarketDto(Long id, String name, MarketStatus status) {}

public class Market {
    private final Long id;
    private final String name;
    // 仅 getter，无 setter
}

// ✅ 返回 Optional 而非 null
Optional<Market> market = marketRepository.findBySlug(slug);
return market.map(MarketResponse::from)
    .orElseThrow(() -> new MarketNotFoundException(slug));
```

### 3.4 异常处理

```java
// ✅ 自定义领域异常
public class MarketNotFoundException extends RuntimeException {
    public MarketNotFoundException(String slug) {
        super("Market not found: " + slug);
    }
}

// ✅ 全局异常处理器
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiError.validation(message));
    }
}
```

### 3.5 日志规范

```java
private static final Logger log = LoggerFactory.getLogger(MarketService.class);

log.info("fetch_market slug={}", slug);
log.error("fetch_market_failed slug={} error={}", slug, ex.getMessage(), ex);
```

### 3.6 REST API 规范

```java
@RestController
@RequestMapping("/api/markets")
@Validated
public class MarketController {
    @GetMapping
    ResponseEntity<Page<MarketResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Market> markets = marketService.list(PageRequest.of(page, size));
        return ResponseEntity.ok(markets.map(MarketResponse::from));
    }

    @PostMapping
    ResponseEntity<MarketResponse> create(
            @Valid @RequestBody CreateMarketRequest request) {
        Market market = marketService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(MarketResponse.from(market));
    }
}
```

### 3.7 DTO/请求验证

```java
public record CreateMarketRequest(
    @NotBlank @Size(max = 200) String name,
    @NotBlank @Size(max = 2000) String description,
    @NotNull @FutureOrPresent Instant endDate
) {}

public record MarketResponse(Long id, String name, MarketStatus status) {
    static MarketResponse from(Market market) {
        return new MarketResponse(market.id(), market.name(), market.status());
    }
}
```

---

## 4. Vue 前端规范

### 4.1 组件规范

```vue
<!-- ✅ 单文件组件结构 -->
<template>
  <div class="market-card">
    <h3>{{ title }}</h3>
    <slot name="description" />
  </div>
</template>

<script setup lang="ts">
// ✅ 使用 defineProps 和 defineEmits
interface Props {
  title: string
  items?: Market[]
}

const props = withDefaults(defineProps<Props>(), {
  items: () => []
})

const emit = defineEmits<{
  (e: 'select', item: Market): void
}>()
</script>

<style scoped>
.market-card {
  padding: 1rem;
}
</style>
```

### 4.2 导入顺序

```typescript
// ✅ 按以下顺序分组
import { computed, ref } from 'vue'           // Vue API
import { useMarketStore } from '@/stores'      // 项目 stores
import { fetchMarkets } from '@/api/markets'   // API
import type { Market } from '@/types'          // 类型定义
import { formatDate } from '@/utils/date'     // 工具函数
```

### 4.3 TypeScript 类型规范

```typescript
// ✅ 使用 interface 定义对象类型
interface Market {
  id: number
  name: string
  status: 'active' | 'inactive'
}

// ✅ 使用 type 定义联合类型/别名
type MarketStatus = 'active' | 'inactive' | 'pending'
type MarketList = Market[]

// ✅ 组件 Props 类型
interface Props {
  modelValue?: string
  items: Market[]
}

// ✅ 事件类型
interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'select', item: Market): void
}
```

### 4.4 组合式 API 规范

```typescript
// ✅ 使用组合式函数封装逻辑
export function useMarkets() {
  const markets = ref<Market[]>([])
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchAll = async () => {
    loading.value = true
    try {
      markets.value = await fetchMarkets()
    } catch (e) {
      error.value = e as Error
    } finally {
      loading.value = false
    }
  }

  return { markets, loading, error, fetchAll }
}

// ✅ computed 缓存计算结果
const activeMarkets = computed(() => 
  markets.value.filter(m => m.status === 'active')
)
```

---

## 5. Git 提交规范

```
feat: 新增可视化爬虫配置功能
fix: 修复爬虫任务列表分页问题
docs: 更新 API 文档
style: 格式化代码，无逻辑变更
refactor: 重构爬虫调度器逻辑
test: 添加单元测试
chore: 更新依赖版本
```

---

## 6. 通用规范

### 6.1 错误处理

- ❌ 禁止 `catch(e) {}` 空捕获
- ✅ 必须记录日志或重新抛出
- ✅ 使用自定义异常类封装领域错误

### 6.2 空值处理

- ❌ 避免返回 `null`，使用 `Optional` (Java) 或 `undefined` (TS)
- ✅ 使用空集合/数组代替 `null`

### 6.3 日志

- ✅ 使用 SLF4J (后端) / console 方法 (前端)
- ✅ 日志级别：ERROR > WARN > INFO > DEBUG
- ✅ 禁止在生产环境使用 `System.out`

### 6.4 代码审查检查项

- [ ] 无硬编码值（配置外）
- [ ] 无未使用的导入/变量
- [ ] 异常被正确处理
- [ ] 单元测试覆盖核心逻辑
- [ ] API 响应格式一致
