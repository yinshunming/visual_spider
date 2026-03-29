package com.visualspider.repository;

import com.visualspider.domain.PageSnapshot;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PageSnapshotMapper {

    @Select("SELECT * FROM page_snapshot WHERE id = #{id}")
    Optional<PageSnapshot> findById(Long id);

    @Select("SELECT * FROM page_snapshot WHERE session_id = #{sessionId}")
    List<PageSnapshot> findBySessionId(Long sessionId);

    @Select("SELECT * FROM page_snapshot ORDER BY created_at DESC")
    List<PageSnapshot> findAll();

    @Insert("INSERT INTO page_snapshot (session_id, url, html_path, screenshot_path, created_at) " +
            "VALUES (#{sessionId}, #{url}, #{htmlPath}, #{screenshotPath}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PageSnapshot pageSnapshot);

    @Delete("DELETE FROM page_snapshot WHERE id = #{id}")
    void deleteById(Long id);

    @Delete("DELETE FROM page_snapshot WHERE session_id = #{sessionId}")
    void deleteBySessionId(Long sessionId);
}
