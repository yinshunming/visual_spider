package com.visualspider.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.web.servlet.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CrawlController - 爬取执行 API")
class CrawlControllerTest extends BaseControllerTest {

    @Test @DisplayName("C1: POST /api/crawl/start/99999 → 400 (invalid task)")
    void testStartInvalidTask() throws Exception {
        when(crawlExecutionService.execute(99999L))
            .thenThrow(new IllegalArgumentException("Task not found: 99999"));
        mockMvc.perform(post("/api/crawl/start/99999"))
                .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("C2: GET /api/crawl/schedules/99999/last-run → 404")
    void testLastRunNotFound() throws Exception {
        mockMvc.perform(get("/api/crawl/schedules/99999/last-run"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("C3: GET /api/crawl/schedules → 200")
    void testListSchedules() throws Exception {
        mockMvc.perform(get("/api/crawl/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}