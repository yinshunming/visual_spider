package com.visualspider.controller;

import com.visualspider.dto.FieldRuleRequest;
import com.visualspider.dto.FieldRuleResponse;
import com.visualspider.service.FieldRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FieldRule Controller - 字段规则 CRUD API
 */
@RestController
@RequestMapping("/api/field-rules")
public class FieldRuleController {

    private static final Logger log = LoggerFactory.getLogger(FieldRuleController.class);
    private final FieldRuleService fieldRuleService;

    public FieldRuleController(FieldRuleService fieldRuleService) {
        this.fieldRuleService = fieldRuleService;
    }

    /**
     * 批量保存字段规则
     * POST /api/field-rules/batch
     * Body: [{fieldCode, selectors, extractType, validations, taskId}, ...]
     */
    @PostMapping("/batch")
    public ResponseEntity<List<Long>> saveBatch(@RequestBody List<FieldRuleRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Long> ids = fieldRuleService.saveBatch(requests);
        log.info("save_batch count={}", ids.size());
        return ResponseEntity.ok(ids);
    }

    /**
     * 查询所有规则
     * GET /api/field-rules
     */
    @GetMapping
    public ResponseEntity<List<FieldRuleResponse>> listAll() {
        return ResponseEntity.ok(fieldRuleService.listAll());
    }

    /**
     * 按 taskId 查询规则
     * GET /api/field-rules?taskId={taskId}
     */
    @GetMapping(params = "taskId")
    public ResponseEntity<List<FieldRuleResponse>> listByTaskId(@RequestParam Long taskId) {
        return ResponseEntity.ok(fieldRuleService.listByTaskId(taskId));
    }

    /**
     * 删除规则
     * DELETE /api/field-rules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fieldRuleService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
