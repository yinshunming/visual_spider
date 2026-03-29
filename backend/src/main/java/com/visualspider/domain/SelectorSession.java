package com.visualspider.domain;

import com.visualspider.dto.NodeSelection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 内存中的 Selector Session
 * 管理浏览器会话和用户选择的 selectors
 */
public class SelectorSession {
    private String id;
    private String url;
    private Object page;  // Playwright Page reference (kept as Object to avoid import)
    private List<NodeSelection> selections;
    private LocalDateTime createdAt;

    public SelectorSession(String url, Object page) {
        this.id = UUID.randomUUID().toString();
        this.url = url;
        this.page = page;
        this.selections = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    // Getters only (immutable selections list after creation)
    public String getId() { return id; }
    public String getUrl() { return url; }
    public Object getPage() { return page; }
    public List<NodeSelection> getSelections() { return selections; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void addSelection(NodeSelection selection) {
        this.selections.add(selection);
    }

    public void removeSelection(int index) {
        if (index >= 0 && index < selections.size()) {
            this.selections.remove(index);
        }
    }
}