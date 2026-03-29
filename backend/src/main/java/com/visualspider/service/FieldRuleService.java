package com.visualspider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visualspider.domain.FieldValidation;
import com.visualspider.domain.FieldRule;
import com.visualspider.dto.FieldRuleRequest;
import com.visualspider.dto.FieldRuleResponse;
import com.visualspider.repository.FieldRuleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * FieldRule Service - CRUD + JSON 序列化 + 校验
 */
@Service
public class FieldRuleService {

    private static final Logger log = LoggerFactory.getLogger(FieldRuleService.class);

    private final FieldRuleMapper fieldRuleMapper;
    private final ObjectMapper objectMapper;

    public FieldRuleService(FieldRuleMapper fieldRuleMapper, ObjectMapper objectMapper) {
        this.fieldRuleMapper = fieldRuleMapper;
        this.objectMapper = objectMapper;
    }

    // ==================== CRUD ====================

    /**
     * 批量保存 FieldRule（从 DTO 序列化）
     */
    public List<Long> saveBatch(List<FieldRuleRequest> requests) {
        LocalDateTime now = LocalDateTime.now();
        return requests.stream().map(req -> {
            FieldRule rule = new FieldRule();
            rule.setFieldCode(req.fieldCode());
            rule.setSelectors(toJson(req.selectors()));
            rule.setExtractType(req.extractType());
            rule.setValidations(toJson(req.validations()));
            rule.setTaskId(req.taskId());
            rule.setCreatedAt(now);
            fieldRuleMapper.insert(rule);
            log.info("saved_field_rule id={} fieldCode={}", rule.getId(), rule.getFieldCode());
            return rule.getId();
        }).toList();
    }

    /**
     * 查询所有规则
     */
    public List<FieldRuleResponse> listAll() {
        List<FieldRule> rules = fieldRuleMapper.findAll();
        return rules.stream().map(this::toResponse).toList();
    }

    /**
     * 按 taskId 查询
     */
    public List<FieldRuleResponse> listByTaskId(Long taskId) {
        List<FieldRule> rules = fieldRuleMapper.findByTaskId(taskId);
        return rules.stream().map(this::toResponse).toList();
    }

    /**
     * 删除规则
     */
    public void deleteById(Long id) {
        fieldRuleMapper.deleteById(id);
        log.info("deleted_field_rule id={}", id);
    }

    // ==================== JSON 序列化 ====================

    private String toJson(Object obj) {
        if (obj == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("json_serialize_failed obj={}", obj, e);
            throw new RuntimeException("JSON serialization failed for: " + obj, e);
        }
    }

    private <T> T parseJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            try {
                return typeRef.getType().equals(String.class) ? (T) "[]" : (T) List.of();
            } catch (Exception e) {
                return (T) List.of();
            }
        }
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("json_parse_failed json={}", json, e);
            return (T) List.of();
        }
    }

    private FieldRuleResponse toResponse(FieldRule rule) {
        return new FieldRuleResponse(
            rule.getId(),
            rule.getFieldCode(),
            parseJson(rule.getSelectors(), new TypeReference<>() {}),
            rule.getExtractType(),
            parseJson(rule.getValidations(), new TypeReference<>() {}),
            rule.getTaskId(),
            rule.getCreatedAt()
        );
    }

    // ==================== 校验逻辑 ====================

    /**
     * 校验提取结果
     * @param value 提取到的值（可能为 null）
     * @param validations 校验规则列表
     * @return 校验错误列表（空 = 校验通过）
     */
    public List<String> validate(String value, List<FieldValidation> validations) {
        if (validations == null || validations.isEmpty()) {
            return List.of();
        }

        List<String> errors = new ArrayList<>();
        for (FieldValidation validation : validations) {
            String validationType = validation.validationType();
            String param = validation.value();

            try {
                switch (validationType) {
                    case "required" -> {
                        if (value == null || value.isBlank()) {
                            errors.add("字段为必填项");
                        }
                    }
                    case "minLength" -> {
                        if (value != null) {
                            int minLen = Integer.parseInt(param);
                            if (value.length() < minLen) {
                                errors.add("内容长度不能少于 " + minLen + " 个字符");
                            }
                        }
                    }
                    case "regex" -> {
                        if (value != null && !value.isBlank()) {
                            Pattern.compile(param);
                            if (!Pattern.matches(param, value)) {
                                errors.add("内容格式不符合正则表达式: " + param);
                            }
                        }
                    }
                    case "datetimeParse" -> {
                        if (value != null && !value.isBlank()) {
                            try {
                                DateTimeFormatter.ofPattern(param).parse(value);
                            } catch (IllegalArgumentException | DateTimeParseException e) {
                                errors.add("日期格式不正确，应为: " + param);
                            }
                        }
                    }
                    default -> log.warn("unknown_validation_type fieldCode={} type={}", validationType, validationType);
                }
            } catch (PatternSyntaxException e) {
                errors.add("正则表达式语法错误: " + param);
            } catch (NumberFormatException e) {
                errors.add("minLength 参数无效: " + param);
            }
        }

        return errors;
    }
}
