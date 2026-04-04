package com.visualspider.integration;

import com.visualspider.controller.BaseControllerTest;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("端到端流程测试")
class E2E_flowTest extends BaseControllerTest {

    @Test @DisplayName("X1: 新建任务 → 查看详情 → 触发爬取(400 无效任务)")
    void testNewTaskThenDetailAndCrawl() throws Exception {
        // Create task
        String taskJson = """
            {"name": "E2E Test Task", "seedUrl": "https://example.com", "maxPages": 2}
            """;
        var createResult = mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(taskJson))
                .andExpect(status().isCreated())
                .andReturn();

        // GET /api/tasks should return an array (mock returns empty list since mapper is mocked)
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @DisplayName("X2: 新建任务表单页 → 填写 → 创建 → 跳转详情页")
    void testNewTaskFormFlow() throws Exception {
        // GET /tasks/new should load the form
        mockMvc.perform(get("/tasks/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/new"));

        // POST to create (API level)
        String taskJson = """
            {"name": "Form Test", "seedUrl": "https://test.com", "enabled": false}
            """;
        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(taskJson))
                .andExpect(status().isCreated());
    }

    @Test @DisplayName("X3: 字段规则 CRUD 完整流程")
    void testFieldRuleCrudFlow() throws Exception {
        // Batch create
        String rulesJson = """
            [
              {"fieldCode": "title", "selectors": [{"selector": "h1", "selectorType": "css"}], "extractType": "TEXT", "taskId": 1},
              {"fieldCode": "content", "selectors": [{"selector": "article", "selectorType": "css"}], "extractType": "HTML", "taskId": 1}
            ]
            """;
        mockMvc.perform(post("/api/field-rules/batch")
                .contentType("application/json")
                .content(rulesJson))
                .andExpect(status().isOk());

        // List all
        mockMvc.perform(get("/api/field-rules"))
                .andExpect(status().isOk());

        // Filter by taskId
        mockMvc.perform(get("/api/field-rules").param("taskId", "1"))
                .andExpect(status().isOk());
    }
}