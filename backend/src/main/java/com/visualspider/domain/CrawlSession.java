package com.visualspider.domain;

import java.time.LocalDateTime;

public class CrawlSession {
    private Long id;
    private Long taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer pagesCrawled;
    private Integer articlesExtracted;
    private String errorMessage;

    public CrawlSession() {}

    public Long getId() { return id; }
    public Long getTaskId() { return taskId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public Integer getPagesCrawled() { return pagesCrawled; }
    public Integer getArticlesExtracted() { return articlesExtracted; }
    public String getErrorMessage() { return errorMessage; }

    public void setId(Long id) { this.id = id; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setStatus(String status) { this.status = status; }
    public void setPagesCrawled(Integer pagesCrawled) { this.pagesCrawled = pagesCrawled; }
    public void setArticlesExtracted(Integer articlesExtracted) { this.articlesExtracted = articlesExtracted; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
