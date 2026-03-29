package com.visualspider.dto;

import java.util.List;

/**
 * 预览结果 DTO
 */
public record PreviewResult(
    String fieldCode,
    String value,
    boolean success,
    List<SelectorAttempt> attemptedSelectors,
    String error,
    List<String> validationErrors
) {
    /**
     * 单个选择器尝试结果
     */
    public record SelectorAttempt(
        String selector,
        String selectorType,
        String value,
        boolean succeeded
    ) {}

    public static PreviewResult success(String fieldCode, String value, List<SelectorAttempt> attempts) {
        return new PreviewResult(fieldCode, value, true, attempts, null, List.of());
    }

    public static PreviewResult failure(String fieldCode, List<SelectorAttempt> attempts, String error) {
        return new PreviewResult(fieldCode, null, false, attempts, error, List.of());
    }

    public PreviewResult withValidationErrors(List<String> validationErrors) {
        return new PreviewResult(fieldCode, value, success, attemptedSelectors, error, validationErrors);
    }
}
