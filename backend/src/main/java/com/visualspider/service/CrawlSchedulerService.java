package com.visualspider.service;

import com.visualspider.domain.CrawlSchedule;
import com.visualspider.domain.CrawlTask;
import com.visualspider.job.CrawlScheduleJob;
import com.visualspider.repository.CrawlTaskMapper;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CrawlSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(CrawlSchedulerService.class);

    private final Scheduler quartzScheduler;
    private final CrawlTaskMapper crawlTaskMapper;

    private final ConcurrentHashMap<Long, CrawlSchedule> schedules = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> runningTasks = new ConcurrentHashMap<>();

    public CrawlSchedulerService(Scheduler quartzScheduler, CrawlTaskMapper crawlTaskMapper) {
        this.quartzScheduler = quartzScheduler;
        this.crawlTaskMapper = crawlTaskMapper;
    }

    @PostConstruct
    void init() {
        List<CrawlTask> tasks = crawlTaskMapper.findAllWithCronExpression();
        for (CrawlTask task : tasks) {
            if (Boolean.TRUE.equals(task.getEnabled())) {
                registerSchedule(task);
            }
        }
    }

    void registerSchedule(CrawlTask task) {
        String cronExpression = task.getCronExpression();
        if (cronExpression == null || cronExpression.isBlank()) {
            return;
        }

        try {
            Long taskId = task.getId();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + taskId, "crawl-triggers")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

            JobDetail jobDetail = JobBuilder.newJob(CrawlScheduleJob.class)
                .withIdentity("job_" + taskId, "crawl-jobs")
                .usingJobData("taskId", taskId)
                .build();

            quartzScheduler.scheduleJob(jobDetail, trigger);

            CrawlSchedule schedule = new CrawlSchedule(
                taskId,
                cronExpression,
                Boolean.TRUE.equals(task.getEnabled()),
                null, null, null, null, null
            );
            schedules.put(taskId, schedule);

            log.info("注册调度任务 taskId={}, cron={}", taskId, cronExpression);
        } catch (SchedulerException e) {
            log.error("注册调度任务失败 taskId={}, cron={}", task.getId(), cronExpression, e);
        }
    }

    void removeSchedule(Long taskId) {
        try {
            JobKey jobKey = JobKey.jobKey("job_" + taskId, "crawl-jobs");
            quartzScheduler.deleteJob(jobKey);

            schedules.remove(taskId);
            runningTasks.remove(taskId);

            log.info("删除调度任务 taskId={}", taskId);
        } catch (SchedulerException e) {
            log.error("删除调度任务失败 taskId={}", taskId, e);
        }
    }

    public boolean tryStart(Long taskId, Long sessionId) {
        Long existing = runningTasks.putIfAbsent(taskId, sessionId);
        if (existing != null) {
            log.warn("调度任务冲突 taskId={}, 已有sessionId={}, 拒绝新sessionId={}", taskId, existing, sessionId);
            return false;
        }

        CrawlSchedule schedule = schedules.get(taskId);
        if (schedule != null) {
            schedules.put(taskId, schedule.withCurrentSession(sessionId));
        }
        return true;
    }

    public void onComplete(Long taskId, Long sessionId, LocalDateTime start, LocalDateTime end, String status) {
        runningTasks.remove(taskId);

        CrawlSchedule schedule = schedules.get(taskId);
        if (schedule != null) {
            schedules.put(taskId, schedule.withLastRun(sessionId, start, end, status));
        }
    }

    CrawlSchedule getSchedule(Long taskId) {
        return schedules.get(taskId);
    }

    public List<CrawlSchedule> getAllSchedules() {
        return List.copyOf(schedules.values());
    }

}
