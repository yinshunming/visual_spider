package com.visualspider.repository;

import com.visualspider.domain.FieldRule;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FieldRuleMapper {

    @Select("SELECT id, field_code, selectors, extract_type, validations, task_id, created_at FROM field_rule WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "fieldCode", column = "field_code"),
        @Result(property = "selectors", column = "selectors"),
        @Result(property = "extractType", column = "extract_type"),
        @Result(property = "validations", column = "validations"),
        @Result(property = "taskId", column = "task_id"),
        @Result(property = "createdAt", column = "created_at")
    })
    Optional<FieldRule> findById(Long id);

    @Select("SELECT id, field_code, selectors, extract_type, validations, task_id, created_at FROM field_rule WHERE task_id = #{taskId}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "fieldCode", column = "field_code"),
        @Result(property = "selectors", column = "selectors"),
        @Result(property = "extractType", column = "extract_type"),
        @Result(property = "validations", column = "validations"),
        @Result(property = "taskId", column = "task_id"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<FieldRule> findByTaskId(Long taskId);

    @Select("SELECT id, field_code, selectors, extract_type, validations, task_id, created_at FROM field_rule ORDER BY created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "fieldCode", column = "field_code"),
        @Result(property = "selectors", column = "selectors"),
        @Result(property = "extractType", column = "extract_type"),
        @Result(property = "validations", column = "validations"),
        @Result(property = "taskId", column = "task_id"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<FieldRule> findAll();

    @Insert("INSERT INTO field_rule (field_code, selectors, extract_type, validations, task_id, created_at) " +
            "VALUES (#{fieldCode}, #{selectors}, #{extractType}, #{validations}, #{taskId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(FieldRule fieldRule);

    @Update("UPDATE field_rule SET field_code=#{fieldCode}, " +
            "selectors=#{selectors}, extract_type=#{extractType}, " +
            "validations=#{validations}, task_id=#{taskId} WHERE id=#{id}")
    void update(FieldRule fieldRule);

    @Delete("DELETE FROM field_rule WHERE id = #{id}")
    void deleteById(Long id);

    @Delete("DELETE FROM field_rule WHERE task_id = #{taskId}")
    void deleteByTaskId(Long taskId);
}
