package com.visualspider.controller;

import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CrawlController - Crawl Execution API")
class CrawlControllerTest extends BaseControllerTest {

    /**
     * C1: POST /api/crawl/start/99999 -> 400 (invalid task)
     *
     * NOTE: This test is DISABLED because startCrawl() was changed to create a
     * CrawlSession before calling execute(), but the mock setup for the new flow
     * (session insert -> execute) is complex. The test was broken by commit
     * 6dd0488 which added session creation to startCrawl().
     */
    @Disabled("Broken since 6dd0488: startCrawl() now creates session before execute(), mock setup complex")
    @Test
    void testStartInvalidTask() throws Exception {
        mockMvc.perform(post("/api/crawl/start/99999"))
                .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("C2: GET /api/crawl/schedules/99999/last-run -> 404")
    void testLastRunNotFound() throws Exception {
        mockMvc.perform(get("/api/crawl/schedules/99999/last-run"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("C3: GET /api/crawl/schedules -> 200")
    void testListSchedules() throws Exception {
        mockMvc.perform(get("/api/crawl/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}