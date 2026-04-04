package com.visualspider.controller;

import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("FieldRuleController - 字段规则 API")
class FieldRuleControllerTest extends BaseControllerTest {

    @Test @DisplayName("F1: GET /api/field-rules → 200")
    void testList() throws Exception {
        mockMvc.perform(get("/api/field-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @DisplayName("F2: GET /api/field-rules?taskId=1 → 200")
    void testListByTask() throws Exception {
        mockMvc.perform(get("/api/field-rules").param("taskId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @DisplayName("F3: POST /api/field-rules/batch → 200")
    void testBatchCreate() throws Exception {
        String json = """
            [
              {"fieldCode": "title", "selectors": [{"selector": "h1.title", "selectorType": "css"}], "extractType": "TEXT", "taskId": 1}
            ]
            """;
        mockMvc.perform(post("/api/field-rules/batch")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("F4: DELETE /api/field-rules/99999 → 200")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/field-rules/99999"))
                .andExpect(status().isOk());
    }
}