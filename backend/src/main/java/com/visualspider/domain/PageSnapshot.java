package com.visualspider.domain;

import java.time.LocalDateTime;

public class PageSnapshot {
    private Long id;
    private Long sessionId;
    private String url;
    private String htmlPath;
    private String screenshotPath;
    private LocalDateTime createdAt;

    public PageSnapshot() {}

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public String getUrl() { return url; }
    public String getHtmlPath() { return htmlPath; }
    public String getScreenshotPath() { return screenshotPath; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public void setUrl(String url) { this.url = url; }
    public void setHtmlPath(String htmlPath) { this.htmlPath = htmlPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
