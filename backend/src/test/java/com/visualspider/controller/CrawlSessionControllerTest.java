package com.visualspider.controller;

import com.visualspider.domain.*;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import java.time.LocalDateTime;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CrawlSessionController - 会话 API")
class CrawlSessionControllerTest extends BaseControllerTest {

    @Test @DisplayName("S1: GET /api/sessions → 200")
    void testList() throws Exception {
        mockMvc.perform(get("/api/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @DisplayName("S2: GET /api/sessions/99999 → 404")
    void testGetNotFound() throws Exception {
        mockMvc.perform(get("/api/sessions/99999"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("S3: GET /api/sessions/task/1 → 200")
    void testGetByTaskId() throws Exception {
        mockMvc.perform(get("/api/sessions/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @DisplayName("S4: POST /api/sessions → 200")
    void testCreate() throws Exception {
        String json = """
            {"taskId": 1, "status": "RUNNING"}
            """;
        mockMvc.perform(post("/api/sessions")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }
}