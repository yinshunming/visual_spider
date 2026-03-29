package com.visualspider.repository;

import com.visualspider.domain.FieldRule;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FieldRuleMapper {

    @Select("SELECT * FROM field_rule WHERE id = #{id}")
    Optional<FieldRule> findById(Long id);

    @Select("SELECT * FROM field_rule WHERE task_id = #{taskId}")
    List<FieldRule> findByTaskId(Long taskId);

    @Select("SELECT * FROM field_rule ORDER BY created_at DESC")
    List<FieldRule> findAll();

    @Insert("INSERT INTO field_rule (field_name, selector, selector_type, extraction_type, attribute_name, task_id, created_at) " +
            "VALUES (#{fieldName}, #{selector}, #{selectorType}, #{extractionType}, #{attributeName}, #{taskId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(FieldRule fieldRule);

    @Update("UPDATE field_rule SET field_name=#{fieldName}, selector=#{selector}, selector_type=#{selectorType}, " +
            "extraction_type=#{extractionType}, attribute_name=#{attributeName}, task_id=#{taskId} WHERE id=#{id}")
    void update(FieldRule fieldRule);

    @Delete("DELETE FROM field_rule WHERE id = #{id}")
    void deleteById(Long id);

    @Delete("DELETE FROM field_rule WHERE task_id = #{taskId}")
    void deleteByTaskId(Long taskId);
}
