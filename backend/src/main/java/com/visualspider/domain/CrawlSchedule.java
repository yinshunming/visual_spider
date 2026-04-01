package com.visualspider.domain;

import java.time.LocalDateTime;

public record CrawlSchedule(
    Long taskId,
    String cronExpression,
    boolean enabled,
    Long currentSessionId,
    Long lastSessionId,
    LocalDateTime lastStartTime,
    LocalDateTime lastEndTime,
    String lastStatus
) {
    public CrawlSchedule withCurrentSession(Long sessionId) {
        return new CrawlSchedule(
            taskId, cronExpression, enabled, sessionId,
            lastSessionId, lastStartTime, lastEndTime, lastStatus
        );
    }

    public CrawlSchedule withLastRun(Long sessionId, LocalDateTime start, LocalDateTime end, String status) {
        return new CrawlSchedule(
            taskId, cronExpression, enabled, null,
            sessionId, start, end, status
        );
    }

    public CrawlSchedule withEnabled(boolean enabled) {
        return new CrawlSchedule(
            taskId, cronExpression, enabled, currentSessionId,
            lastSessionId, lastStartTime, lastEndTime, lastStatus
        );
    }
}
