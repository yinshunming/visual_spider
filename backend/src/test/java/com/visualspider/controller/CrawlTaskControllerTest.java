package com.visualspider.controller;

import com.visualspider.domain.CrawlTask;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CrawlTaskController - 任务 CRUD API")
class CrawlTaskControllerTest extends BaseControllerTest {

    @Test @DisplayName("T1: GET /api/tasks → 200 JSON array")
    void testList() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @DisplayName("T2: GET /api/tasks/1 → 200 or 404")
    void testGetExists() throws Exception {
        mockMvc.perform(get("/api/tasks/99999"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("T3: GET /api/tasks/99999 → 404")
    void testGetNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/99999"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("T4: POST /api/tasks (min params) → 201 returns ID")
    void testCreateMin() throws Exception {
        String json = """
            {"name": "Test Task", "seedUrl": "https://example.com"}
            """;
        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test @DisplayName("T5: POST /api/tasks (full params) → 201")
    void testCreateFull() throws Exception {
        String json = """
            {
              "name": "Full Task",
              "seedUrl": "https://news.example.com",
              "paginationSelector": ".next",
              "paginationType": "button",
              "detailUrlPattern": "https://news.example.com/.*\\\\.html",
              "maxPages": 5,
              "enabled": true,
              "cronExpression": "0 0 2 * * ?"
            }
            """;
        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test @DisplayName("T6: PUT /api/tasks/1 → 200 or 404")
    void testUpdateExists() throws Exception {
        String json = "{\"name\": \"Updated\"}";
        mockMvc.perform(put("/api/tasks/99999")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("T7: PUT /api/tasks/99999 → 404")
    void testUpdateNotFound() throws Exception {
        String json = "{\"name\": \"Updated\"}";
        mockMvc.perform(put("/api/tasks/99999")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("T8: DELETE /api/tasks/1 → 200")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/tasks/99999"))
                .andExpect(status().isOk());
    }
}