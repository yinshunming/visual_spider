package com.visualspider.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 字段校验规则 record
 * @param validationType  校验类型：required | minLength | regex | datetimeParse
 * @param value           校验参数（如最小长度、正则表达式、日期格式）
 */
public record FieldValidation(
    @JsonProperty("validationType") String validationType,
    @JsonProperty("value") String value
) {
    public FieldValidation {
        Objects.requireNonNull(validationType, "validationType must not be null");
    }

    public static FieldValidation required() {
        return new FieldValidation("required", null);
    }

    public static FieldValidation minLength(int length) {
        return new FieldValidation("minLength", String.valueOf(length));
    }

    public static FieldValidation regex(String pattern) {
        return new FieldValidation("regex", pattern);
    }

    public static FieldValidation datetimeParse(String format) {
        return new FieldValidation("datetimeParse", format);
    }
}
