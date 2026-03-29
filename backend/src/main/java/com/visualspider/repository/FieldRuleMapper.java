package com.visualspider.repository;

import com.visualspider.domain.ExtractType;
import com.visualspider.domain.FieldRule;
import com.visualspider.domain.FieldValidation;
import com.visualspider.domain.SelectorDef;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FieldRuleMapper {

    @Select("SELECT * FROM field_rule WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "fieldCode", column = "field_code"),
        @Result(property = "selectors", column = "selectors",
            typeHandler = SelectorDefListTypeHandler.class),
        @Result(property = "extractType", column = "extract_type"),
        @Result(property = "validations", column = "validations",
            typeHandler = FieldValidationListTypeHandler.class),
        @Result(property = "taskId", column = "task_id"),
        @Result(property = "createdAt", column = "created_at")
    })
    Optional<FieldRule> findById(Long id);

    @Select("SELECT * FROM field_rule WHERE task_id = #{taskId}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "fieldCode", column = "field_code"),
        @Result(property = "selectors", column = "selectors",
            typeHandler = SelectorDefListTypeHandler.class),
        @Result(property = "extractType", column = "extract_type"),
        @Result(property = "validations", column = "validations",
            typeHandler = FieldValidationListTypeHandler.class),
        @Result(property = "taskId", column = "task_id"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<FieldRule> findByTaskId(Long taskId);

    @Select("SELECT * FROM field_rule ORDER BY created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "fieldCode", column = "field_code"),
        @Result(property = "selectors", column = "selectors",
            typeHandler = SelectorDefListTypeHandler.class),
        @Result(property = "extractType", column = "extract_type"),
        @Result(property = "validations", column = "validations",
            typeHandler = FieldValidationListTypeHandler.class),
        @Result(property = "taskId", column = "task_id"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<FieldRule> findAll();

    @Insert("INSERT INTO field_rule (field_code, selectors, extract_type, validations, task_id, created_at) " +
            "VALUES (#{fieldCode}, #{selectors, typeHandler=SelectorDefListTypeHandler}, " +
            "#{extractType}, #{validations, typeHandler=FieldValidationListTypeHandler}, " +
            "#{taskId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(FieldRule fieldRule);

    @Update("UPDATE field_rule SET field_code=#{fieldCode}, " +
            "selectors=#{selectors, typeHandler=SelectorDefListTypeHandler}, " +
            "extract_type=#{extractType}, " +
            "validations=#{validations, typeHandler=FieldValidationListTypeHandler}, " +
            "task_id=#{taskId} WHERE id=#{id}")
    void update(FieldRule fieldRule);

    @Delete("DELETE FROM field_rule WHERE id = #{id}")
    void deleteById(Long id);

    @Delete("DELETE FROM field_rule WHERE task_id = #{taskId}")
    void deleteByTaskId(Long taskId);
}
