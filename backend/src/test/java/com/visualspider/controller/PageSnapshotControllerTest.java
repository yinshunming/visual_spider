package com.visualspider.controller;

import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PageSnapshotController - 快照 API")
class PageSnapshotControllerTest extends BaseControllerTest {

    @Test @DisplayName("N1: GET /api/snapshots → 200")
    void testList() throws Exception {
        mockMvc.perform(get("/api/snapshots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @DisplayName("N2: GET /api/snapshots/99999 → 404")
    void testGetNotFound() throws Exception {
        mockMvc.perform(get("/api/snapshots/99999"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("N3: GET /api/snapshots/session/1 → 200")
    void testListBySession() throws Exception {
        mockMvc.perform(get("/api/snapshots/session/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @DisplayName("N4: POST /api/snapshots → 201")
    void testCreate() throws Exception {
        String json = """
            {"sessionId": 1, "url": "https://example.com", "htmlPath": "1/a.html"}
            """;
        mockMvc.perform(post("/api/snapshots")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test @DisplayName("N5: DELETE /api/snapshots/99999 → 200")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/snapshots/99999"))
                .andExpect(status().isOk());
    }
}