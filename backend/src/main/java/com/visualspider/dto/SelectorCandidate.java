package com.visualspider.dto;

/**
 * Selector 候选 DTO
 */
public class SelectorCandidate {
    private String selector;
    private String selectorType;
    private String description;

    public SelectorCandidate() {}

    public SelectorCandidate(String selector, String selectorType, String description) {
        this.selector = selector;
        this.selectorType = selectorType;
        this.description = description;
    }

    public String getSelector() { return selector; }
    public void setSelector(String selector) { this.selector = selector; }

    public String getSelectorType() { return selectorType; }
    public void setSelectorType(String selectorType) { this.selectorType = selectorType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
