package com.visualspider.controller;

import com.microsoft.playwright.Page;
import com.visualspider.domain.CrawlSchedule;
import com.visualspider.domain.CrawlSession;
import com.visualspider.domain.CrawlTask;
import com.visualspider.repository.CrawlSessionMapper;
import com.visualspider.repository.CrawlTaskMapper;
import com.visualspider.service.CrawlExecutionService;
import com.visualspider.service.CrawlSchedulerService;
import com.visualspider.service.SnapshotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Crawl Controller - M4 爬取执行 API
 *
 * 提供手动触发爬取任务的接口
 */
@RestController
@RequestMapping("/api/crawl")
public class CrawlController {

    private final CrawlExecutionService crawlExecutionService;
    private final CrawlSchedulerService schedulerService;
    private final CrawlSessionMapper crawlSessionMapper;
    private final CrawlTaskMapper crawlTaskMapper;
    private final SnapshotService snapshotService;

    public CrawlController(
            CrawlExecutionService crawlExecutionService,
            CrawlSchedulerService schedulerService,
            CrawlSessionMapper crawlSessionMapper,
            CrawlTaskMapper crawlTaskMapper,
            SnapshotService snapshotService) {
        this.crawlExecutionService = crawlExecutionService;
        this.schedulerService = schedulerService;
        this.crawlSessionMapper = crawlSessionMapper;
        this.crawlTaskMapper = crawlTaskMapper;
        this.snapshotService = snapshotService;
    }

    /**
     * 手动触发爬取任务
     * POST /api/crawl/start/{taskId}
     *
     * @param taskId 任务 ID
     * @return 爬取结果
     */
    @PostMapping("/start/{taskId}")
    public ResponseEntity<?> startCrawl(@PathVariable Long taskId) {
        try {
            var result = crawlExecutionService.execute(taskId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "pagesCrawled", result.pagesCrawled(),
                "articlesExtracted", result.articlesExtracted(),
                "errors", result.errors()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Crawl failed: " + e.getMessage()
            ));
        }
    }

    // ==================== M5 Schedule Management Endpoints ====================

    /**
     * 立即执行一次爬取任务（跳过重叠检测）
     * POST /api/crawl/schedules/{taskId}/run-once
     */
    @PostMapping("/schedules/{taskId}/run-once")
    public ResponseEntity<?> runOnce(@PathVariable Long taskId) {
        // 创建 audit session
        CrawlSession session = new CrawlSession();
        session.setTaskId(taskId);
        session.setStartTime(LocalDateTime.now());
        session.setStatus("RUNNING");
        crawlSessionMapper.insert(session);
        Long sessionId = session.getId();

        // 重叠检测
        boolean started = schedulerService.tryStart(taskId, sessionId);
        if (!started) {
            session.setEndTime(LocalDateTime.now());
            session.setStatus("SKIPPED");
            session.setErrorMessage("重叠执行被跳过");
            crawlSessionMapper.update(session);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "error", "Task is already running"
            ));
        }

        // 构建快照回调
        Consumer<Page> snapshotCallback = page -> {
            snapshotService.saveSnapshot(page, sessionId, page.url());
        };

        // 执行爬取
        CrawlExecutionService.CrawlResult result;
        try {
            result = crawlExecutionService.execute(taskId, sessionId, snapshotCallback);
        } catch (Exception e) {
            session.setEndTime(LocalDateTime.now());
            session.setStatus("FAILED");
            session.setErrorMessage(e.getMessage());
            crawlSessionMapper.update(session);
            schedulerService.onComplete(taskId, sessionId,
                session.getStartTime(), session.getEndTime(), session.getStatus());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "sessionId", sessionId,
                "error", "Crawl failed: " + e.getMessage()
            ));
        }

        // 更新 session
        session.setEndTime(LocalDateTime.now());
        session.setPagesCrawled(result.pagesCrawled());
        session.setArticlesExtracted(result.articlesExtracted());
        session.setStatus(result.errors().isEmpty() ? "SUCCESS" : "FAILED");
        session.setErrorMessage(result.errors().isEmpty() ? null : String.join("; ", result.errors()));
        crawlSessionMapper.update(session);

        // 通知 scheduler 完成
        schedulerService.onComplete(taskId, sessionId,
            session.getStartTime(), session.getEndTime(), session.getStatus());

        return ResponseEntity.accepted().body(Map.of(
            "success", true,
            "sessionId", sessionId,
            "taskId", taskId,
            "pagesCrawled", result.pagesCrawled(),
            "articlesExtracted", result.articlesExtracted(),
            "errors", result.errors()
        ));
    }

    /**
     * 获取最近一次执行状态
     * GET /api/schedules/{taskId}/last-run
     */
    @GetMapping("/schedules/{taskId}/last-run")
    public ResponseEntity<?> getLastRun(@PathVariable Long taskId) {
        CrawlSession lastRun = crawlSessionMapper.findLastByTaskId(taskId);
        if (lastRun == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "No run found for taskId: " + taskId
            ));
        }
        return ResponseEntity.ok(Map.of(
            "id", lastRun.getId(),
            "taskId", lastRun.getTaskId(),
            "startTime", lastRun.getStartTime(),
            "endTime", lastRun.getEndTime(),
            "status", lastRun.getStatus(),
            "pagesCrawled", lastRun.getPagesCrawled(),
            "articlesExtracted", lastRun.getArticlesExtracted(),
            "errorMessage", lastRun.getErrorMessage()
        ));
    }

    /**
     * 列出所有调度任务
     * GET /api/schedules
     */
    @GetMapping("/schedules")
    public ResponseEntity<?> listSchedules() {
        List<CrawlSchedule> schedules = schedulerService.getAllSchedules();
        List<Map<String, Object>> result = schedules.stream().map(schedule -> {
            String taskName = crawlTaskMapper.findById(schedule.taskId())
                .map(CrawlTask::getName)
                .orElse("Unknown");
            return Map.<String, Object>of(
                "taskId", schedule.taskId(),
                "taskName", taskName,
                "cronExpression", schedule.cronExpression(),
                "enabled", schedule.enabled(),
                "isRunning", schedule.currentSessionId() != null,
                "lastRun", schedule.lastSessionId() != null ? Map.of(
                    "sessionId", schedule.lastSessionId(),
                    "startTime", schedule.lastStartTime(),
                    "endTime", schedule.lastEndTime(),
                    "status", schedule.lastStatus()
                ) : null
            );
        }).toList();
        return ResponseEntity.ok(result);
    }
}
