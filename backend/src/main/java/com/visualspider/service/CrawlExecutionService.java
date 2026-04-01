package com.visualspider.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.visualspider.domain.Article;
import com.visualspider.domain.ExtractType;
import com.visualspider.domain.FieldRule;
import com.visualspider.domain.FieldValidation;
import com.visualspider.domain.SelectorDef;
import com.visualspider.repository.ArticleMapper;
import com.visualspider.repository.CrawlSessionMapper;
import com.visualspider.repository.CrawlTaskMapper;
import com.visualspider.repository.FieldRuleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CrawlExecutionService - M4 递归翻页抓取执行器
 * 
 * 流程：
 * 1. 加载 CrawlTask + FieldRule[]
 * 2. 打开 Playwright Page，导航到 seedUrl
 * 3. 循环（最多 maxPages 次）：
 *    a. 提取当前页所有链接，过滤出匹配 detailUrlPattern 的详情页 URL（去重）
 *    b. 对每个未抓取的详情页：新建 Page → 用 FieldRule[] 提取字段 → 校验 → 保存 Article
 *    c. 如果设置了 paginationSelector，点击翻页按钮
 * 4. 返回抓取结果
 */
@Service
public class CrawlExecutionService {

    private static final Logger log = LoggerFactory.getLogger(CrawlExecutionService.class);

    private final Browser browser;
    private final CrawlTaskMapper crawlTaskMapper;
    private final FieldRuleMapper fieldRuleMapper;
    private final FieldRuleService fieldRuleService;
    private final ArticleMapper articleMapper;
    private final CrawlSessionMapper crawlSessionMapper;
    private final ObjectMapper objectMapper;
    private final java.util.function.Consumer<Page> onPageCrawled;

    public CrawlExecutionService(Browser browser,
                                 CrawlTaskMapper crawlTaskMapper,
                                 FieldRuleMapper fieldRuleMapper,
                                 FieldRuleService fieldRuleService,
                                 ArticleMapper articleMapper,
                                 CrawlSessionMapper crawlSessionMapper,
                                 ObjectMapper objectMapper,
                                 java.util.function.Consumer<Page> onPageCrawled) {
        this.browser = browser;
        this.crawlTaskMapper = crawlTaskMapper;
        this.fieldRuleMapper = fieldRuleMapper;
        this.fieldRuleService = fieldRuleService;
        this.articleMapper = articleMapper;
        this.crawlSessionMapper = crawlSessionMapper;
        this.objectMapper = objectMapper;
        this.onPageCrawled = onPageCrawled;
    }

    // ==================== 公开接口 ====================

    /**
     * 执行爬取任务（无回调）
     * @param taskId 任务 ID
     * @return 爬取结果
     */
    public CrawlResult execute(Long taskId) {
        return execute(taskId, null);
    }

    /**
     * 执行爬取任务
     * @param taskId 任务 ID
     * @param onPageCrawled 页面抓取回调（可为空）
     * @return 爬取结果
     */
    public CrawlResult execute(Long taskId, java.util.function.Consumer<Page> onPageCrawled) {
        log.info("crawl_execute_start taskId={}", taskId);

        // 1. 加载任务
        var taskOpt = crawlTaskMapper.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        var task = taskOpt.get();

        // 2. 加载字段规则
        List<FieldRule> fieldRules = fieldRuleMapper.findByTaskId(taskId);
        if (fieldRules.isEmpty()) {
            log.warn("crawl_execute_no_rules taskId={}", taskId);
        }

        // 3. 解析规则（JSON → POJO）
        List<ParsedFieldRule> parsedRules = fieldRules.stream()
                .map(this::parseFieldRule)
                .toList();

        // 4. 初始化状态
        Set<String> seenUrls = new HashSet<>();
        int pagesCrawled = 0;
        int articlesExtracted = 0;
        List<String> errors = new ArrayList<>();
        int maxPages = (task.getMaxPages() != null && task.getMaxPages() > 0) ? task.getMaxPages() : 10;
        String seedUrl = task.getSeedUrl();
        String detailUrlPattern = task.getDetailUrlPattern();
        String paginationSelector = task.getPaginationSelector();

        Page listPage = null;
        try {
            // 5. 打开首页
            listPage = browser.newPage();
            listPage.navigate(seedUrl);
            listPage.waitForLoadState(LoadState.NETWORKIDLE);
            if (onPageCrawled != null) {
                onPageCrawled.accept(listPage);
            }
            log.info("crawl_navigated_to url={}", seedUrl);

            // 6. 翻页循环
            for (int pageNum = 0; pageNum < maxPages; pageNum++) {
                pagesCrawled++;
                log.info("crawl_page_start taskId={} pageNum={}", taskId, pageNum + 1);

                // 6a. 提取当前列表页所有链接
                List<String> detailUrls = extractDetailUrls(listPage, detailUrlPattern);
                log.info("crawl_urls_found pageNum={} count={}", pageNum + 1, detailUrls.size());

                // 6b. 遍历每个详情页 URL
                for (String detailUrl : detailUrls) {
                    if (seenUrls.contains(detailUrl)) {
                        continue;
                    }
                    seenUrls.add(detailUrl);

                    // 检查是否已抓取过（数据库去重）
                    if (articleMapper.findByUrl(detailUrl).isPresent()) {
                        log.debug("crawl_url_already_exists url={}", detailUrl);
                        continue;
                    }

                    try {
                        // 在当前 tab 导航到详情页（也可以用 new Page() 开新标签）
                        Article article = extractArticle(listPage, detailUrl, parsedRules, task.getName(), onPageCrawled);
                        if (article != null) {
                            articleMapper.insert(article);
                            articlesExtracted++;
                            log.info("crawl_article_saved url={} title={}", 
                                detailUrl, 
                                article.getTitle() != null ? article.getTitle().substring(0, Math.min(50, article.getTitle().length())) : "N/A");
                        }
                    } catch (Exception e) {
                        log.error("crawl_article_failed url={} error={}", detailUrl, e.getMessage());
                        errors.add("Failed to extract [" + detailUrl + "]: " + e.getMessage());
                    }
                }

                // 6c. 翻页
                if (pageNum < maxPages - 1 && paginationSelector != null && !paginationSelector.isBlank()) {
                    boolean hasNext = clickPagination(listPage, paginationSelector);
                    if (!hasNext) {
                        log.info("crawl_no_more_pages taskId={}", taskId);
                        break;
                    }
                } else {
                    break;
                }
            }

        } catch (PlaywrightException e) {
            log.error("crawl_browser_error taskId={} error={}", taskId, e.getMessage());
            errors.add("Browser error: " + e.getMessage());
        } finally {
            if (listPage != null) {
                listPage.close();
            }
        }

        log.info("crawl_execute_complete taskId={} pages={} articles={} errors={}", 
            taskId, pagesCrawled, articlesExtracted, errors.size());
        return new CrawlResult(pagesCrawled, articlesExtracted, errors);
    }

    /**
     * 执行爬取任务（带 session 跟踪）
     * @param taskId 任务 ID
     * @param sessionId session ID
     * @return 爬取结果
     */
    public CrawlResult execute(Long taskId, Long sessionId, java.util.function.Consumer<Page> onPageCrawled) {
        log.info("crawl_execute_with_session taskId={} sessionId={}", taskId, sessionId);

        var sessionOpt = crawlSessionMapper.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("CrawlSession not found: " + sessionId);
        }
        var session = sessionOpt.get();

        // 更新 session 状态为 RUNNING
        session.setStartTime(LocalDateTime.now());
        session.setStatus("RUNNING");
        crawlSessionMapper.update(session);

        CrawlResult result;
        try {
            result = execute(taskId, onPageCrawled);
        } catch (Exception e) {
            log.error("crawl_execute_session_failed taskId={} sessionId={} error={}", 
                taskId, sessionId, e.getMessage());
            session.setEndTime(LocalDateTime.now());
            session.setStatus("FAILED");
            session.setErrorMessage(e.getMessage());
            crawlSessionMapper.update(session);
            throw e;
        }

        // 更新 session 为 SUCCESS
        session.setEndTime(LocalDateTime.now());
        session.setStatus(result.errors().isEmpty() ? "SUCCESS" : "FAILED");
        session.setPagesCrawled(result.pagesCrawled());
        session.setArticlesExtracted(result.articlesExtracted());
        session.setErrorMessage(result.errors().isEmpty() ? null : String.join("; ", result.errors()));
        crawlSessionMapper.update(session);

        return result;
    }

    // ==================== 内部方法 ====================

    /**
     * 从列表页提取所有匹配 detailUrlPattern 的详情页 URL
     */
    private List<String> extractDetailUrls(Page page, String detailUrlPattern) {
        List<String> urls = new ArrayList<>();
        if (detailUrlPattern == null || detailUrlPattern.isBlank()) {
            return urls;
        }

        try {
            Pattern regex = Pattern.compile(detailUrlPattern);
            // 获取页面所有 <a> 标签的 href
            Locator links = page.locator("a[href]");
            int count = links.count();
            for (int i = 0; i < count; i++) {
                String href = links.nth(i).getAttribute("href");
                if (href != null) {
                    // 补全相对 URL
                    String fullUrl = URI.create(page.url()).resolve(href).toString();
                    Matcher m = regex.matcher(fullUrl);
                    if (m.find()) {
                        urls.add(fullUrl);
                    }
                }
            }
        } catch (Exception e) {
            log.error("extract_detail_urls_failed pattern={} error={}", detailUrlPattern, e.getMessage());
        }
        return urls;
    }

    /**
     * 提取单个详情页的 Article
     * 
     * @param listPage 当前列表页（用于导航到详情页）
     * @param detailUrl 详情页 URL
     * @param parsedRules 已解析的字段规则
     * @param taskName 任务名（作为 source）
     */
    private Article extractArticle(Page listPage, String detailUrl, 
                                   List<ParsedFieldRule> parsedRules, String taskName,
                                   java.util.function.Consumer<Page> onPageCrawled) {
        // 导航到详情页
        listPage.navigate(detailUrl);
        listPage.waitForLoadState(LoadState.NETWORKIDLE);
        if (onPageCrawled != null) {
            onPageCrawled.accept(listPage);
        }

        Article article = new Article();
        article.setUrl(detailUrl);
        article.setSource(taskName);
        article.setStatus("CRAWLED");
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());

        boolean hasContent = false;

        for (ParsedFieldRule rule : parsedRules) {
            String fieldCode = rule.fieldCode;
            String extracted = extractField(listPage, rule.selectors, rule.extractType, rule.attributeName);

            // 校验
            List<String> validationErrors = fieldRuleService.validate(extracted, rule.validations);
            if (!validationErrors.isEmpty()) {
                log.debug("field_validation_failed fieldCode={} url={} errors={}", 
                    fieldCode, detailUrl, validationErrors);
            }

            // 设置字段
            switch (fieldCode) {
                case "title" -> {
                    article.setTitle(extracted);
                    if (extracted != null && !extracted.isBlank()) hasContent = true;
                }
                case "content" -> {
                    article.setContent(extracted);
                    if (extracted != null && !extracted.isBlank()) hasContent = true;
                }
                case "author" -> article.setAuthor(extracted);
                case "publish_date" -> {
                    if (extracted != null && !extracted.isBlank()) {
                        article.setPublishDate(parseDate(extracted));
                    }
                }
                case "source" -> {
                    if (extracted != null && !extracted.isBlank()) {
                        article.setSource(extracted);
                    }
                }
                default -> log.warn("unknown_field_code fieldCode={}", fieldCode);
            }
        }

        return hasContent ? article : null;
    }

    /**
     * 使用选择器提取字段值
     */
    private String extractField(Page page, List<SelectorDef> selectors, 
                                ExtractType extractType, String attributeName) {
        for (SelectorDef def : selectors) {
            try {
                String selector = def.selector();
                String selectorType = def.selectorType();
                Locator locator;

                if ("XPATH".equalsIgnoreCase(selectorType)) {
                    locator = page.locator("xpath=" + selector);
                } else {
                    locator = page.locator(selector);
                }

                int count = locator.count();
                if (count > 0) {
                    String value = switch (extractType) {
                        case TEXT -> locator.first().textContent();
                        case HTML -> locator.first().innerHTML();
                        case ATTR -> {
                            if (attributeName != null && !attributeName.isBlank()) {
                                yield locator.first().getAttribute(attributeName);
                            }
                            yield null;
                        }
                    };

                    if (value != null && !value.isBlank()) {
                        return value.trim();
                    }
                }
            } catch (PlaywrightException e) {
                log.debug("extract_field_selector_failed selector={} error={}", def.selector(), e.getMessage());
            }
        }
        return null;
    }

    /**
     * 点击翻页按钮
     * @return true 表示还有下一页，false 表示已到最后一页
     */
    private boolean clickPagination(Page page, String paginationSelector) {
        try {
            Locator btn = page.locator(paginationSelector);
            if (btn.count() == 0) {
                return false;
            }
            btn.first().click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
            return true;
        } catch (PlaywrightException e) {
            log.debug("pagination_click_failed selector={} error={}", paginationSelector, e.getMessage());
            return false;
        }
    }

    /**
     * 解析日期字符串（支持多种格式）
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        String s = dateStr.trim();
        try {
            // 尝试常见格式
            if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(s);
            }
            if (s.matches("\\d{4}/\\d{2}/\\d{2}")) {
                return LocalDate.parse(s.replace("/", "-"));
            }
            // yyyy年MM月dd日
            if (s.matches("\\d{4}年\\d{1,2}月\\d{1,2}日")) {
                s = s.replace("年", "-").replace("月", "-").replace("日", "");
                return LocalDate.parse(s);
            }
        } catch (Exception e) {
            log.debug("parse_date_failed str={}", dateStr);
        }
        return null;
    }

    /**
     * 解析 FieldRule 中的 JSON 字段
     */
    private ParsedFieldRule parseFieldRule(FieldRule rule) {
        List<SelectorDef> selectors = parseJson(rule.getSelectors(), new TypeReference<>() {});
        List<FieldValidation> validations = parseJson(rule.getValidations(), new TypeReference<>() {});
        ExtractType extractType;
        try {
            extractType = rule.getExtractType() != null 
                ? ExtractType.valueOf(rule.getExtractType().toUpperCase()) 
                : ExtractType.TEXT;
        } catch (IllegalArgumentException e) {
            extractType = ExtractType.TEXT;
        }
        String attributeName = selectors.isEmpty() ? null : 
            (selectors.get(0).selectorType().equalsIgnoreCase("ATTR") ? selectors.get(0).selector() : null);
        
        return new ParsedFieldRule(
            rule.getFieldCode(),
            selectors,
            extractType,
            attributeName,
            validations
        );
    }

    private <T> T parseJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            try {
                return typeRef.getType().equals(String.class) ? (T) "[]" : (T) List.of();
            } catch (Exception e) {
                return (T) List.of();
            }
        }
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("json_parse_failed json={}", json, e);
            try {
                return typeRef.getType().equals(String.class) ? (T) "[]" : (T) List.of();
            } catch (Exception ex) {
                return (T) List.of();
            }
        }
    }

    // ==================== 内部类型 ====================

    /**
     * 解析后的字段规则（JSON POJO）
     */
    private record ParsedFieldRule(
        String fieldCode,
        List<SelectorDef> selectors,
        ExtractType extractType,
        String attributeName,
        List<FieldValidation> validations
    ) {}

    /**
     * 爬取结果
     */
    public record CrawlResult(
        int pagesCrawled,
        int articlesExtracted,
        List<String> errors
    ) {}
}