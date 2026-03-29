package com.visualspider.controller;

import com.visualspider.domain.FieldRule;
import com.visualspider.repository.FieldRuleMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/field-rules")
public class FieldRuleController {

    private final FieldRuleMapper fieldRuleMapper;

    public FieldRuleController(FieldRuleMapper fieldRuleMapper) {
        this.fieldRuleMapper = fieldRuleMapper;
    }

    @GetMapping
    public List<FieldRule> list() {
        return fieldRuleMapper.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FieldRule> get(@PathVariable Long id) {
        return fieldRuleMapper.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/task/{taskId}")
    public List<FieldRule> getByTaskId(@PathVariable Long taskId) {
        return fieldRuleMapper.findByTaskId(taskId);
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody FieldRule fieldRule) {
        fieldRule.setCreatedAt(LocalDateTime.now());
        fieldRuleMapper.insert(fieldRule);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody FieldRule fieldRule) {
        return fieldRuleMapper.findById(id)
                .map(existing -> {
                    fieldRule.setId(id);
                    fieldRuleMapper.update(fieldRule);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fieldRuleMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
