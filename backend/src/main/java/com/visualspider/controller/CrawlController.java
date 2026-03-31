package com.visualspider.controller;

import com.visualspider.service.CrawlExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Crawl Controller - M4 爬取执行 API
 * 
 * 提供手动触发爬取任务的接口
 */
@RestController
@RequestMapping("/api/crawl")
public class CrawlController {

    private final CrawlExecutionService crawlExecutionService;

    public CrawlController(CrawlExecutionService crawlExecutionService) {
        this.crawlExecutionService = crawlExecutionService;
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
}
