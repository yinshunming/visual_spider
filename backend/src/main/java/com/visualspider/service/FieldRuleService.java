package com.visualspider.service;

import com.visualspider.domain.ExtractType;
import com.visualspider.domain.FieldValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 字段规则校验服务
 */
@Service
public class FieldRuleService {

    private static final Logger log = LoggerFactory.getLogger(FieldRuleService.class);

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
