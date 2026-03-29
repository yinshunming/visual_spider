package com.visualspider.controller;

import com.visualspider.domain.FieldRule;
import com.visualspider.repository.FieldRuleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FieldRule Controller - 字段规则 CRUD API
 */
@RestController
@RequestMapping("/api/field-rules")
public class FieldRuleController {

    private static final Logger log = LoggerFactory.getLogger(FieldRuleController.class);
    private final FieldRuleMapper fieldRuleMapper;

    public FieldRuleController(FieldRuleMapper fieldRuleMapper) {
        this.fieldRuleMapper = fieldRuleMapper;
    }

    /**
     * 批量保存字段规则
     * POST /api/field-rules/batch
     * Body: [{fieldCode, selectors, extractType, validations, taskId}, ...]
     */
    @PostMapping("/batch")
    public ResponseEntity<List<Long>> saveBatch(@RequestBody List<FieldRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Long> ids = rules.stream().map(rule -> {
            rule.setCreatedAt(LocalDateTime.now());
            fieldRuleMapper.insert(rule);
            log.info("saved_field_rule id={} fieldCode={}", rule.getId(), rule.getFieldCode());
            return rule.getId();
        }).toList();

        return ResponseEntity.ok(ids);
    }

    /**
     * 查询所有规则
     * GET /api/field-rules
     */
    @GetMapping
    public ResponseEntity<List<FieldRule>> listAll() {
        return ResponseEntity.ok(fieldRuleMapper.findAll());
    }

    /**
     * 按 taskId 查询规则
     * GET /api/field-rules?taskId={taskId}
     */
    @GetMapping(params = "taskId")
    public ResponseEntity<List<FieldRule>> listByTaskId(@RequestParam Long taskId) {
        return ResponseEntity.ok(fieldRuleMapper.findByTaskId(taskId));
    }

    /**
     * 删除规则
     * DELETE /api/field-rules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fieldRuleMapper.deleteById(id);
        log.info("deleted_field_rule id={}", id);
        return ResponseEntity.ok().build();
    }
}
