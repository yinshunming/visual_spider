package com.visualspider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visualspider.domain.FieldRule;
import com.visualspider.domain.FieldValidation;
import com.visualspider.dto.FieldRuleRequest;
import com.visualspider.dto.FieldRuleResponse;
import com.visualspider.repository.FieldRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FieldRuleServiceTest {

    @Mock
    private FieldRuleMapper fieldRuleMapper;

    private ObjectMapper objectMapper;

    private FieldRuleService fieldRuleService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        fieldRuleService = new FieldRuleService(fieldRuleMapper, objectMapper);
    }

    // ==================== validate() tests ====================

    @Nested
    @DisplayName("validate() - required")
    class ValidateRequired {

        @Test
        @DisplayName("V1: required — null value → returns error '字段为必填项'")
        void required_nullValue_returnsError() {
            List<String> errors = fieldRuleService.validate(
                null,
                List.of(FieldValidation.required())
            );
            assertThat(errors).containsExactly("字段为必填项");
        }

        @Test
        @DisplayName("V2: required — blank string → returns error")
        void required_blankString_returnsError() {
            List<String> errors = fieldRuleService.validate(
                "   ",
                List.of(FieldValidation.required())
            );
            assertThat(errors).containsExactly("字段为必填项");
        }

        @Test
        @DisplayName("V3: required — non-blank → returns empty list (pass)")
        void required_nonBlank_returnsEmpty() {
            List<String> errors = fieldRuleService.validate(
                "some value",
                List.of(FieldValidation.required())
            );
            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("V4: required — null value with null validations list → returns empty (no validation = pass)")
        void required_nullValidationsList_returnsEmpty() {
            List<String> errors = fieldRuleService.validate(null, null);
            assertThat(errors).isEmpty();
        }
    }

    @Nested
    @DisplayName("validate() - minLength")
    class ValidateMinLength {

        @Test
        @DisplayName("V5: minLength — value too short → returns error")
        void minLength_tooShort_returnsError() {
            List<String> errors = fieldRuleService.validate(
                "ab",
                List.of(FieldValidation.minLength(5))
            );
            assertThat(errors).containsExactly("内容长度不能少于 5 个字符");
        }

        @Test
        @DisplayName("V6: minLength — value exactly min → returns empty (pass)")
        void minLength_exactlyMin_returnsEmpty() {
            List<String> errors = fieldRuleService.validate(
                "abcde",
                List.of(FieldValidation.minLength(5))
            );
            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("V7: minLength — value too short but value is null → returns empty (null value skips check)")
        void minLength_nullValue_returnsEmpty() {
            List<String> errors = fieldRuleService.validate(
                null,
                List.of(FieldValidation.minLength(5))
            );
            assertThat(errors).isEmpty();
        }
    }

    @Nested
    @DisplayName("validate() - regex")
    class ValidateRegex {

        @Test
        @DisplayName("V8: regex — matching → returns empty")
        void regex_matching_returnsEmpty() {
            List<String> errors = fieldRuleService.validate(
                "abc123",
                List.of(FieldValidation.regex("\\w+\\d+"))
            );
            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("V9: regex — non-matching → returns error")
        void regex_nonMatching_returnsError() {
            List<String> errors = fieldRuleService.validate(
                "abc",
                List.of(FieldValidation.regex("\\d+"))
            );
            assertThat(errors).containsExactly("内容格式不符合正则表达式: \\d+");
        }

        @Test
        @DisplayName("V10: regex — null value → returns empty (skipped)")
        void regex_nullValue_returnsEmpty() {
            List<String> errors = fieldRuleService.validate(
                null,
                List.of(FieldValidation.regex("\\d+"))
            );
            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("V11: regex — invalid pattern syntax → returns error '正则表达式语法错误'")
        void regex_invalidPattern_returnsError() {
            List<String> errors = fieldRuleService.validate(
                "anything",
                List.of(FieldValidation.regex("*invalid"))
            );
            assertThat(errors).containsExactly("正则表达式语法错误: *invalid");
        }
    }

    @Nested
    @DisplayName("validate() - datetimeParse")
    class ValidateDatetimeParse {

        @Test
        @DisplayName("V12: datetimeParse — valid format → returns empty")
        void datetimeParse_validFormat_returnsEmpty() {
            List<String> errors = fieldRuleService.validate(
                "2024-01-01",
                List.of(FieldValidation.datetimeParse("yyyy-MM-dd"))
            );
            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("V13: datetimeParse — invalid format → returns error")
        void datetimeParse_invalidFormat_returnsError() {
            List<String> errors = fieldRuleService.validate(
                "2024-13-01",
                List.of(FieldValidation.datetimeParse("yyyy-MM-dd"))
            );
            assertThat(errors).containsExactly("日期格式不正确，应为: yyyy-MM-dd");
        }

        @Test
        @DisplayName("V14: datetimeParse — null value → returns empty")
        void datetimeParse_nullValue_returnsEmpty() {
            List<String> errors = fieldRuleService.validate(
                null,
                List.of(FieldValidation.datetimeParse("yyyy-MM-dd"))
            );
            assertThat(errors).isEmpty();
        }
    }

    @Nested
    @DisplayName("validate() - multiple validations")
    class ValidateMultiple {

        @Test
        @DisplayName("V15: multiple validations, all pass → returns empty")
        void multipleAllPass_returnsEmpty() {
            List<FieldValidation> validations = List.of(
                FieldValidation.required(),
                FieldValidation.minLength(3),
                FieldValidation.regex("\\w+")
            );
            List<String> errors = fieldRuleService.validate("abc", validations);
            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("V16: multiple validations, one fails → returns one error")
        void multipleOneFails_returnsOneError() {
            List<FieldValidation> validations = List.of(
                FieldValidation.required(),
                FieldValidation.minLength(10)
            );
            List<String> errors = fieldRuleService.validate("abc", validations);
            assertThat(errors).hasSize(1);
            assertThat(errors).containsExactly("内容长度不能少于 10 个字符");
        }

        @Test
        @DisplayName("V17: unknown validation type → logged, returns empty")
        void unknownValidationType_loggedAndReturnsEmpty() {
            List<FieldValidation> validations = List.of(
                new FieldValidation("unknownType", "someValue")
            );
            List<String> errors = fieldRuleService.validate("any", validations);
            assertThat(errors).isEmpty();
        }
    }

    // ==================== saveBatch() tests ====================

    @Nested
    @DisplayName("saveBatch()")
    class SaveBatch {

        @Test
        @DisplayName("V18: saves one rule → mapper.insert() called once with correct fieldCode/extractType/taskId")
        void saveBatch_oneRule_insertCalledOnce() {
            FieldRuleRequest request = new FieldRuleRequest(
                "title",
                null,
                "CSS",
                List.of(FieldValidation.required()),
                100L
            );
            doAnswer(invocation -> {
                FieldRule rule = invocation.getArgument(0);
                rule.setId(1L);
                return null;
            }).when(fieldRuleMapper).insert(any(FieldRule.class));

            List<Long> ids = fieldRuleService.saveBatch(List.of(request));

            assertThat(ids).containsExactly(1L);

            ArgumentCaptor<FieldRule> captor = ArgumentCaptor.forClass(FieldRule.class);
            verify(fieldRuleMapper, times(1)).insert(captor.capture());

            FieldRule captured = captor.getValue();
            assertThat(captured.getFieldCode()).isEqualTo("title");
            assertThat(captured.getExtractType()).isEqualTo("CSS");
            assertThat(captured.getTaskId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("V19: saves two rules → mapper.insert() called twice")
        void saveBatch_twoRules_insertCalledTwice() {
            FieldRuleRequest req1 = new FieldRuleRequest("f1", null, "CSS", null, 1L);
            FieldRuleRequest req2 = new FieldRuleRequest("f2", null, "XPATH", null, 1L);
            doAnswer(invocation -> {
                FieldRule rule = invocation.getArgument(0);
                rule.setId(1L);
                return null;
            }).when(fieldRuleMapper).insert(any(FieldRule.class));

            List<Long> ids = fieldRuleService.saveBatch(List.of(req1, req2));

            assertThat(ids).hasSize(2);
            verify(fieldRuleMapper, times(2)).insert(any(FieldRule.class));
        }

        @Test
        @DisplayName("V20: null selectors → toJson returns '[]'")
        void saveBatch_nullSelectors_serializesToEmptyArray() {
            FieldRuleRequest request = new FieldRuleRequest(
                "field",
                null,  // null selectors
                "CSS",
                null,
                1L
            );
            doAnswer(invocation -> {
                FieldRule rule = invocation.getArgument(0);
                rule.setId(1L);
                return null;
            }).when(fieldRuleMapper).insert(any(FieldRule.class));

            fieldRuleService.saveBatch(List.of(request));

            ArgumentCaptor<FieldRule> captor = ArgumentCaptor.forClass(FieldRule.class);
            verify(fieldRuleMapper).insert(captor.capture());

            assertThat(captor.getValue().getSelectors()).isEqualTo("[]");
        }
    }

    // ==================== listAll() tests ====================

    @Nested
    @DisplayName("listAll()")
    class ListAll {

        @Test
        @DisplayName("V21: returns response list mapped from mapper.findAll()")
        void listAll_returnsMappedList() {
            FieldRule rule = new FieldRule();
            rule.setId(1L);
            rule.setFieldCode("title");
            rule.setSelectors("[]");
            rule.setExtractType("CSS");
            rule.setValidations("[]");
            rule.setTaskId(100L);
            rule.setCreatedAt(LocalDateTime.now());
            when(fieldRuleMapper.findAll()).thenReturn(List.of(rule));

            List<FieldRuleResponse> responses = fieldRuleService.listAll();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).id()).isEqualTo(1L);
            assertThat(responses.get(0).fieldCode()).isEqualTo("title");
            assertThat(responses.get(0).extractType()).isEqualTo("CSS");
        }
    }

    // ==================== listByTaskId() tests ====================

    @Nested
    @DisplayName("listByTaskId()")
    class ListByTaskId {

        @Test
        @DisplayName("V22: delegates to mapper.findByTaskId(taskId)")
        void listByTaskId_delegatesToMapper() {
            FieldRule rule = new FieldRule();
            rule.setId(2L);
            rule.setFieldCode("content");
            rule.setSelectors("[]");
            rule.setExtractType("XPATH");
            rule.setValidations("[]");
            rule.setTaskId(50L);
            rule.setCreatedAt(LocalDateTime.now());
            when(fieldRuleMapper.findByTaskId(50L)).thenReturn(List.of(rule));

            List<FieldRuleResponse> responses = fieldRuleService.listByTaskId(50L);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).id()).isEqualTo(2L);
            verify(fieldRuleMapper).findByTaskId(50L);
        }
    }

    // ==================== deleteById() tests ====================

    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("V23: calls mapper.deleteById(id)")
        void deleteById_callsMapperDeleteById() {
            fieldRuleService.deleteById(42L);

            verify(fieldRuleMapper).deleteById(42L);
        }
    }
}
