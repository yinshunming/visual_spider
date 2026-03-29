package com.visualspider.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 选择器定义 record
 * @param selector  选择器表达式（如 CSS 或 XPath）
 * @param selectorType  选择器类型（CSS / XPATH / ATTR）
 */
public record SelectorDef(
    @JsonProperty("selector") String selector,
    @JsonProperty("selectorType") String selectorType
) {
    public SelectorDef {
        Objects.requireNonNull(selector, "selector must not be null");
        Objects.requireNonNull(selectorType, "selectorType must not be null");
    }

    public static SelectorDef of(String selector, String selectorType) {
        return new SelectorDef(selector, selectorType);
    }
}
