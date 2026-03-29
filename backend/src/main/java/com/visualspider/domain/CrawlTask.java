package com.visualspider.domain;

import java.time.LocalDateTime;

public class CrawlTask {
    private Long id;
    private String name;
    private String seedUrl;
    private String paginationSelector;
    private String paginationType;
    private String detailUrlPattern;
    private Integer maxPages;
    private Boolean enabled;
    private String cronExpression;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CrawlTask() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSeedUrl() { return seedUrl; }
    public String getPaginationSelector() { return paginationSelector; }
    public String getPaginationType() { return paginationType; }
    public String getDetailUrlPattern() { return detailUrlPattern; }
    public Integer getMaxPages() { return maxPages; }
    public Boolean getEnabled() { return enabled; }
    public String getCronExpression() { return cronExpression; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSeedUrl(String seedUrl) { this.seedUrl = seedUrl; }
    public void setPaginationSelector(String paginationSelector) { this.paginationSelector = paginationSelector; }
    public void setPaginationType(String paginationType) { this.paginationType = paginationType; }
    public void setDetailUrlPattern(String detailUrlPattern) { this.detailUrlPattern = detailUrlPattern; }
    public void setMaxPages(Integer maxPages) { this.maxPages = maxPages; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
