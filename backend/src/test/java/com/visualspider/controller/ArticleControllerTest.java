package com.visualspider.controller;

import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ArticleController - 文章 API")
class ArticleControllerTest extends BaseControllerTest {

    @Test @DisplayName("A1: GET /api/articles → 200")
    void testList() throws Exception {
        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @DisplayName("A2: GET /api/articles/99999 → 404")
    void testGetNotFound() throws Exception {
        mockMvc.perform(get("/api/articles/99999"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("A3: POST /api/articles → 201")
    void testCreate() throws Exception {
        String json = """
            {"title": "Test Article", "url": "https://example.com/article/1"}
            """;
        mockMvc.perform(post("/api/articles")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test @DisplayName("A4: PUT /api/articles/99999 → 404")
    void testUpdateNotFound() throws Exception {
        String json = "{\"title\": \"Updated\"}";
        mockMvc.perform(put("/api/articles/99999")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("A5: DELETE /api/articles/99999 → 200")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/articles/99999"))
                .andExpect(status().isOk());
    }
}