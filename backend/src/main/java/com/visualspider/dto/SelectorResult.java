package com.visualspider.dto;

import java.util.List;

/**
 * Selector 选择结果 DTO
 */
public class SelectorResult {
    private String sessionId;
    private String url;
    private List<NodeSelection> selections;

    public SelectorResult() {}

    public SelectorResult(String sessionId, String url, List<NodeSelection> selections) {
        this.sessionId = sessionId;
        this.url = url;
        this.selections = selections;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public List<NodeSelection> getSelections() { return selections; }
    public void setSelections(List<NodeSelection> selections) { this.selections = selections; }
}
