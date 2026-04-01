package com.visualspider.job;

import com.microsoft.playwright.Page;
import com.visualspider.domain.CrawlSession;
import com.visualspider.repository.CrawlSessionMapper;
import com.visualspider.service.CrawlExecutionService;
import com.visualspider.service.CrawlSchedulerService;
import com.visualspider.service.SnapshotService;
import org.quartz.Job;
import java.util.function.Consumer;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quartz Job — 定时触发爬取任务并记录审计日志
 *
 * <p>流程：
 * <ol>
 *   <li>从 JobDataMap 提取 taskId</li>
 *   <li>创建 audit session（状态 RUNNING）</li>
 *   <li>检查是否重叠执行，是则 SKIPPED</li>
 *   <li>执行爬取，成功则 SUCCESS，失败则 FAILED</li>
 *   <li>更新 session 并通知 scheduler 完成</li>
 * </ol>
 *
 * <p><b>注意：</b>Quartz Job 实例每次执行都是新实例，Spring 通过 JobFactory 注入依赖，
 * 所以本类不能有实例字段存储状态。
 */
public class CrawlScheduleJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(CrawlScheduleJob.class);

    private CrawlSchedulerService schedulerService;
    private CrawlExecutionService executionService;
    private CrawlSessionMapper sessionMapper;
    private SnapshotService snapshotService;

    /**
     * Spring 通过 Quartz SpringBeanJobFactory 注入依赖
     */
    public CrawlScheduleJob(CrawlSchedulerService schedulerService,
                            CrawlExecutionService executionService,
                            CrawlSessionMapper sessionMapper,
                            SnapshotService snapshotService) {
        this.schedulerService = schedulerService;
        this.executionService = executionService;
        this.sessionMapper = sessionMapper;
        this.snapshotService = snapshotService;
    }

    /** Quartz 要求无参构造器 */
    public CrawlScheduleJob() {}

    @Override
    public void execute(JobExecutionContext context) {
        Long taskId = context.getJobDetail().getJobDataMap().getLong("taskId");
        log.info("Quartz 触发爬取任务 taskId={}", taskId);

        // Step 1 — 创建 audit session
        CrawlSession session = new CrawlSession();
        session.setTaskId(taskId);
        session.setStartTime(java.time.LocalDateTime.now());
        session.setStatus("RUNNING");
        sessionMapper.insert(session);
        Long sessionId = session.getId();

        // Step 2 — 重叠执行检查
        if (!schedulerService.tryStart(taskId, sessionId)) {
            session.setEndTime(java.time.LocalDateTime.now());
            session.setStatus("SKIPPED");
            session.setErrorMessage("重叠执行被跳过");
            sessionMapper.update(session);
            return;
        }

        // Step 3 — 执行爬取
        try {
            Consumer<Page> snapshotCallback = page -> {
                snapshotService.saveSnapshot(page, sessionId, page.url());
            };
            CrawlExecutionService.CrawlResult result = executionService.execute(taskId, sessionId, snapshotCallback);
            session.setEndTime(java.time.LocalDateTime.now());
            session.setPagesCrawled(result.pagesCrawled());
            session.setArticlesExtracted(result.articlesExtracted());
            session.setStatus("SUCCESS");
        } catch (Exception e) {
            log.error("执行爬取失败 taskId={}", taskId, e);
            session.setEndTime(java.time.LocalDateTime.now());
            session.setStatus("FAILED");
            session.setErrorMessage(e.getMessage());
        }

        // Step 4 — 更新 session
        sessionMapper.update(session);

        // Step 5 — 通知完成
        schedulerService.onComplete(taskId, sessionId,
            session.getStartTime(), session.getEndTime(), session.getStatus());
    }
}
