package com.visualspider.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visualspider.repository.*;
import com.visualspider.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller 测试基类
 * 使用 @WebMvcTest 切片，只加载 Web 层
 * 所有 Mapper/Service 使用 @MockBean Mock
 */
@WebMvcTest
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ─── Mock all Mapper beans ─────────────────────────────────────
    @MockBean protected ArticleMapper articleMapper;
    @MockBean protected CrawlTaskMapper crawlTaskMapper;
    @MockBean protected CrawlSessionMapper crawlSessionMapper;
    @MockBean protected FieldRuleMapper fieldRuleMapper;
    @MockBean protected PageSnapshotMapper pageSnapshotMapper;

    // ─── Mock all Service beans ───────────────────────────────────
    @MockBean protected SelectorService selectorService;
    @MockBean protected CrawlSchedulerService crawlSchedulerService;
    @MockBean protected CrawlExecutionService crawlExecutionService;
    @MockBean protected SnapshotService snapshotService;
    @MockBean protected FieldRuleService fieldRuleService;
}
