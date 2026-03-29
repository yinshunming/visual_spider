package com.visualspider.dto;

import com.visualspider.domain.ExtractType;
import com.visualspider.domain.FieldValidation;
import com.visualspider.domain.SelectorDef;
import java.util.List;
import java.util.Map;

/**
 * 节点选择 DTO - 用户点击后传递的数据
 */
public class NodeSelection {
    private String tagName;
    private String id;
    private String className;
    private String textContent;
    private Map<String, String> attributes;
    private List<SelectorCandidate> candidates;  // 原始候选列表

    // M3 新增：完整规则结构
    private String fieldCode;                     // 字段代码（如 title, content）
    private List<SelectorDef> selectors;          // 选择器数组（多个候选项）
    private ExtractType extractType;              // TEXT / HTML / ATTR
    private String attributeName;                 // 属性名（ATTR 类型时使用）
    private List<FieldValidation> validations;     // 校验规则数组

    // 兼容旧字段（前端确认时使用 selectedSelector）
    private String selectedSelector;
    private String selectorType;

    public NodeSelection() {}

    // Getters and Setters
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }

    public List<SelectorCandidate> getCandidates() { return candidates; }
    public void setCandidates(List<SelectorCandidate> candidates) { this.candidates = candidates; }

    // 兼容旧字段
    public String getSelectedSelector() { return selectedSelector; }
    public void setSelectedSelector(String selectedSelector) { this.selectedSelector = selectedSelector; }

    public String getSelectorType() { return selectorType; }
    public void setSelectorType(String selectorType) { this.selectorType = selectorType; }

    public String getExtractionType() { return extractType != null ? extractType.name() : null; }
    public void setExtractionType(String extractionType) {
        if (extractionType != null) {
            this.extractType = ExtractType.valueOf(extractionType);
        }
    }

    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }

    // M3 新增字段
    public String getFieldCode() { return fieldCode; }
    public void setFieldCode(String fieldCode) { this.fieldCode = fieldCode; }

    public List<SelectorDef> getSelectors() { return selectors; }
    public void setSelectors(List<SelectorDef> selectors) { this.selectors = selectors; }

    public ExtractType getExtractTypeEnum() { return extractType; }
    public void setExtractTypeEnum(ExtractType extractType) { this.extractType = extractType; }

    public List<FieldValidation> getValidations() { return validations; }
    public void setValidations(List<FieldValidation> validations) { this.validations = validations; }
}
