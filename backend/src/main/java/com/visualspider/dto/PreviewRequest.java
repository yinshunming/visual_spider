package com.visualspider.dto;

import com.visualspider.domain.ExtractType;
import com.visualspider.domain.FieldValidation;
import com.visualspider.domain.SelectorDef;
import java.util.List;

/**
 * 预览请求 DTO
 */
public record PreviewRequest(
    String sessionId,
    String fieldCode,
    List<SelectorDef> selectors,
    ExtractType extractType,
    String attributeName,
    List<FieldValidation> validations
) {}
