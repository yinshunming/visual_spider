package com.visualspider.controller;

import com.visualspider.domain.*;
import com.visualspider.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.web.servlet.*;
import org.springframework.boot.test.mock.mockito.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PageController - 页面路由")
class PageControllerTest extends BaseControllerTest {

    // Helper: build a minimal CrawlTask
    private CrawlTask makeTask(Long id, String name) {
        CrawlTask t = new CrawlTask();
        t.setId(id);
        t.setName(name);
        t.setSeedUrl("https://example.com");
        t.setEnabled(false);
        t.setMaxPages(10);
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        return t;
    }

    // Helper: build a minimal CrawlSession
    private CrawlSession makeSession(Long id, Long taskId) {
        CrawlSession s = new CrawlSession();
        s.setId(id);
        s.setTaskId(taskId);
        s.setStartTime(LocalDateTime.now());
        s.setStatus("COMPLETED");
        s.setPagesCrawled(5);
        s.setArticlesExtracted(3);
        return s;
    }

    @Test @DisplayName("P1: GET / → 200")
    void testIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test @DisplayName("P2: GET /tasks → 200")
    void testTasksList() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/index"));
    }

    @Test @DisplayName("P3: GET /tasks/new → 200")
    void testNewTask() throws Exception {
        mockMvc.perform(get("/tasks/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/new"));
    }

    @Test @DisplayName("P4: GET /tasks/1 → 302 redirect (task not found in mock)")
    void testTaskDetailExists() throws Exception {
        // Default mock returns empty → controller redirects to /tasks
        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));
    }

    @Test @DisplayName("P5: GET /tasks/99999 → 302 redirect (not found)")
    void testTaskDetailNotFound() throws Exception {
        mockMvc.perform(get("/tasks/99999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));
    }

    @Test @DisplayName("P6: GET /tasks/1/run → 302 → /editor/1")
    void testRunTask() throws Exception {
        mockMvc.perform(get("/tasks/1/run"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editor/1"));
    }

    @Test @DisplayName("P7: GET /articles → 200")
    void testArticles() throws Exception {
        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(view().name("articles/index"));
    }

    @Test @DisplayName("P8: GET /sessions → 200")
    void testSessions() throws Exception {
        mockMvc.perform(get("/sessions"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/index"));
    }

    @Test @DisplayName("P9: GET /sessions/5 → 302 redirect (session not found in mock)")
    void testSessionDetailExists() throws Exception {
        mockMvc.perform(get("/sessions/5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"));
    }

    @Test @DisplayName("P10: GET /sessions/99999 → 302 (not found)")
    void testSessionDetailNotFound() throws Exception {
        mockMvc.perform(get("/sessions/99999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"));
    }

    @Test @DisplayName("P11: GET /sessions/detail?sessionId=5 → 302 redirect (session not found in mock)")
    void testSessionDetailLegacy() throws Exception {
        mockMvc.perform(get("/sessions/detail").param("sessionId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"));
    }

    @Test @DisplayName("P12: GET /editor → 200")
    void testEditor() throws Exception {
        mockMvc.perform(get("/editor"))
                .andExpect(status().isOk())
                .andExpect(view().name("editor"));
    }

    @Test @DisplayName("P13: GET /editor/1 → 200")
    void testEditorWithTask() throws Exception {
        mockMvc.perform(get("/editor/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editor"));
    }

    @Test @DisplayName("P14: GET /sessions/files/999/xxx → 404 (file not found)")
    void testSnapshotFileNotFound() throws Exception {
        mockMvc.perform(get("/sessions/files/999/nonexistent.html"))
                .andExpect(status().isNotFound());
    }
}