package com.visualspider.domain;

import java.time.LocalDateTime;

public class FieldRule {
    private Long id;
    private String fieldName;
    private String selector;
    private String selectorType;
    private String extractionType;
    private String attributeName;
    private Long taskId;
    private LocalDateTime createdAt;

    public FieldRule() {}

    public FieldRule(Long id, String fieldName, String selector, String selectorType,
                     String extractionType, String attributeName, Long taskId, LocalDateTime createdAt) {
        this.id = id;
        this.fieldName = fieldName;
        this.selector = selector;
        this.selectorType = selectorType;
        this.extractionType = extractionType;
        this.attributeName = attributeName;
        this.taskId = taskId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getFieldName() { return fieldName; }
    public String getSelector() { return selector; }
    public String getSelectorType() { return selectorType; }
    public String getExtractionType() { return extractionType; }
    public String getAttributeName() { return attributeName; }
    public Long getTaskId() { return taskId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public void setSelector(String selector) { this.selector = selector; }
    public void setSelectorType(String selectorType) { this.selectorType = selectorType; }
    public void setExtractionType(String extractionType) { this.extractionType = extractionType; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
