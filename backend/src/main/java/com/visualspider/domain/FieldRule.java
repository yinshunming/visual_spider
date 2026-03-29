package com.visualspider.domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字段规则 Entity
 * 对应 field_rule 表
 */
public class FieldRule {

    private Long id;
    private String fieldCode;               // 字段代码，如 title, content, author
    private List<SelectorDef> selectors;     // 选择器数组（支持多个候选项按顺序尝试）
    private ExtractType extractType;         // TEXT / HTML / ATTR
    private List<FieldValidation> validations; // 校验规则数组
    private Long taskId;                     // 关联的抓取任务 ID
    private LocalDateTime createdAt;

    public FieldRule() {}

    public FieldRule(Long id, String fieldCode, List<SelectorDef> selectors,
                     ExtractType extractType, List<FieldValidation> validations,
                     Long taskId, LocalDateTime createdAt) {
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

    public List<SelectorDef> getSelectors() { return selectors; }
    public void setSelectors(List<SelectorDef> selectors) { this.selectors = selectors; }

    public ExtractType getExtractType() { return extractType; }
    public void setExtractType(ExtractType extractType) { this.extractType = extractType; }

    public List<FieldValidation> getValidations() { return validations; }
    public void setValidations(List<FieldValidation> validations) { this.validations = validations; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * 从 NodeSelection 构建 FieldRule（用于会话确认后的规则转换）
     */
    public static FieldRule fromNodeSelection(String fieldCode, List<SelectorDef> selectors,
                                              ExtractType extractType, List<FieldValidation> validations) {
        return new FieldRule(null, fieldCode, selectors, extractType, validations, null, LocalDateTime.now());
    }
}
