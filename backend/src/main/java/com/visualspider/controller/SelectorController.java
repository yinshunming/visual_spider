package com.visualspider.controller;

import com.visualspider.domain.FieldRule;
import com.visualspider.domain.FieldValidation;
import com.visualspider.domain.SelectorDef;
import com.visualspider.dto.NodeSelection;
import com.visualspider.dto.PreviewRequest;
import com.visualspider.dto.PreviewResult;
import com.visualspider.dto.SelectorResult;
import com.visualspider.repository.FieldRuleMapper;
import com.visualspider.service.SelectorService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Selector Controller - 提供选择器会话 API
 */
@RestController
@RequestMapping("/api/selector")
public class SelectorController {

    private final SelectorService selectorService;
    private final FieldRuleMapper fieldRuleMapper;

    public SelectorController(SelectorService selectorService, FieldRuleMapper fieldRuleMapper) {
        this.selectorService = selectorService;
        this.fieldRuleMapper = fieldRuleMapper;
    }

    /**
     * 启动选择会话
     * POST /api/selector/start
     * Body: {"url": "..."}
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startSession(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        }
        try {
            String sessionId = selectorService.startSession(url);
            return ResponseEntity.ok(Map.of("sessionId", sessionId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to start session: " + e.getMessage()));
        }
    }

    /**
     * 接收浏览器点击事件
     * POST /api/selector/session/{id}/select
     * Body: NodeSelection JSON
     */
    @PostMapping("/session/{id}/select")
    public ResponseEntity<Void> addSelection(@PathVariable String id, @RequestBody NodeSelection selection) {
        selectorService.addSelection(id, selection);
        return ResponseEntity.ok().build();
    }

    /**
     * 确认选择（支持 M3 完整规则结构）
     * POST /api/selector/session/{id}/confirm
     * Body: {index, fieldCode, selectors: [{selector, selectorType}, ...], extractType, attributeName, validations: [{validationType, value}, ...]}
     */
    @PostMapping("/session/{id}/confirm")
    public ResponseEntity<Void> confirmSelection(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {

        int index = Integer.parseInt(request.get("index").toString());
        String fieldCode = (String) request.get("fieldCode");

        // 解析 selectors 数组
        List<SelectorDef> selectors = null;
        Object selectorsObj = request.get("selectors");
        if (selectorsObj instanceof List<?> list) {
            selectors = list.stream().map(item -> {
                @SuppressWarnings("unchecked")
                Map<String, String> m = (Map<String, String>) item;
                return new SelectorDef(m.get("selector"), m.get("selectorType"));
            }).toList();
        }

        String extractionType = (String) request.get("extractionType");
        String attributeName = (String) request.get("attributeName");

        // 解析 validations 数组
        List<FieldValidation> validations = null;
        Object validationsObj = request.get("validations");
        if (validationsObj instanceof List<?> list && !list.isEmpty()) {
            validations = list.stream().map(item -> {
                @SuppressWarnings("unchecked")
                Map<String, String> m = (Map<String, String>) item;
                return new FieldValidation(m.get("validationType"), m.get("value"));
            }).toList();
        }

        selectorService.confirmSelection(id, index, fieldCode, extractionType, attributeName, selectors, validations);
        return ResponseEntity.ok().build();
    }

    /**
     * 预览提取结果
     * POST /api/selector/preview
     * Body: PreviewRequest JSON
     */
    @PostMapping("/preview")
    public ResponseEntity<PreviewResult> previewExtraction(@RequestBody PreviewRequest request) {
        if (request.sessionId() == null || request.sessionId().isBlank()) {
            return ResponseEntity.badRequest()
                .body(PreviewResult.failure(request.fieldCode(), List.of(), "sessionId is required"));
        }
        if (request.selectors() == null || request.selectors().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(PreviewResult.failure(request.fieldCode(), List.of(), "selectors cannot be empty"));
        }
        PreviewResult result = selectorService.previewExtraction(request);
        return ResponseEntity.ok(result);
    }

    /**
     * SSE 事件流
     * GET /api/selector/session/{id}/events
     */
    @GetMapping(value = "/session/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter eventStream(@PathVariable String id) {
        return selectorService.createEmitter(id);
    }

    /**
     * 完成会话，返回结果
     * POST /api/selector/session/{id}/complete
     */
    @PostMapping("/session/{id}/complete")
    public ResponseEntity<SelectorResult> completeSession(@PathVariable String id) {
        SelectorResult result = selectorService.completeSession(id);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 关闭会话
     * DELETE /api/selector/session/{id}
     */
    @DeleteMapping("/session/{id}")
    public ResponseEntity<Void> closeSession(@PathVariable String id) {
        selectorService.closeSession(id);
        return ResponseEntity.ok().build();
    }
}
