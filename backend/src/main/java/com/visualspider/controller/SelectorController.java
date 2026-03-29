package com.visualspider.controller;

import com.visualspider.dto.NodeSelection;
import com.visualspider.dto.SelectorResult;
import com.visualspider.service.SelectorService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * Selector Controller - 提供选择器会话 API
 */
@RestController
@RequestMapping("/api/selector")
public class SelectorController {

    private final SelectorService selectorService;

    public SelectorController(SelectorService selectorService) {
        this.selectorService = selectorService;
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
     * 确认选择
     * POST /api/selector/session/{id}/confirm
     * Body: {index, fieldName, extractionType, attributeName}
     */
    @PostMapping("/session/{id}/confirm")
    public ResponseEntity<Void> confirmSelection(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        
        int index = Integer.parseInt(request.get("index"));
        String fieldName = request.get("fieldName");
        String extractionType = request.get("extractionType");
        String attributeName = request.get("attributeName");
        
        selectorService.confirmSelection(id, index, fieldName, extractionType, attributeName);
        return ResponseEntity.ok().build();
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