package com.visualspider.dto;

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
    private List<SelectorCandidate> candidates;
    private String selectedSelector;
    private String selectorType;
    private String extractionType;
    private String attributeName;
    private String fieldName;

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

    public String getSelectedSelector() { return selectedSelector; }
    public void setSelectedSelector(String selectedSelector) { this.selectedSelector = selectedSelector; }

    public String getSelectorType() { return selectorType; }
    public void setSelectorType(String selectorType) { this.selectorType = selectorType; }

    public String getExtractionType() { return extractionType; }
    public void setExtractionType(String extractionType) { this.extractionType = extractionType; }

    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
}
