package com.visualspider.dto;

import com.visualspider.domain.FieldValidation;
import com.visualspider.domain.SelectorDef;
import java.util.List;

/**
 * 字段规则请求 DTO（批量保存时接收）
 */
public record FieldRuleRequest(
    String fieldCode,
    List<SelectorDef> selectors,
    String extractType,
    List<FieldValidation> validations,
    Long taskId
) {}
