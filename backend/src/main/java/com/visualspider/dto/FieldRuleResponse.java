package com.visualspider.dto;

import com.visualspider.domain.FieldValidation;
import com.visualspider.domain.SelectorDef;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字段规则响应 DTO（读取时从 DB String 反序列化）
 */
public record FieldRuleResponse(
    Long id,
    String fieldCode,
    List<SelectorDef> selectors,
    String extractType,
    List<FieldValidation> validations,
    Long taskId,
    LocalDateTime createdAt
) {
    // SelectorDef DTO for JSON deserialization
    public record SelectorDefDto(String selector, String selectorType) {}

    // FieldValidation DTO for JSON deserialization
    public record FieldValidationDto(String validationType, String value) {}
}
