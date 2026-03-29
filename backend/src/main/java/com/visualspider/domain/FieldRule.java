package com.visualspider.domain;

import java.time.LocalDateTime;

/**
 * 字段规则 Entity
 * 对应 field_rule 表
 */
public class FieldRule {

    private Long id;
    private String fieldCode;
    private String selectors;      // JSON string of List<SelectorDef>
    private String extractType;    // ExtractType enum as string
    private String validations;    // JSON string of List<FieldValidation>
    private Long taskId;
    private LocalDateTime createdAt;

    public FieldRule() {}

    public FieldRule(Long id, String fieldCode, String selectors, String extractType,
                     String validations, Long taskId, LocalDateTime createdAt) {
        this.id = id;
        this.fieldCode = fieldCode;
        this.selectors = selectors;
        this.extractType = extractType;
        this.validations = validations;
        this.taskId = taskId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFieldCode() { return fieldCode; }
    public void setFieldCode(String fieldCode) { this.fieldCode = fieldCode; }

    public String getSelectors() { return selectors; }
    public void setSelectors(String selectors) { this.selectors = selectors; }

    public String getExtractType() { return extractType; }
    public void setExtractType(String extractType) { this.extractType = extractType; }

    public String getValidations() { return validations; }
    public void setValidations(String validations) { this.validations = validations; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
