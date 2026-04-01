package com.visualspider.repository;

import com.visualspider.domain.CrawlSession;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CrawlSessionMapper {

    @Select("SELECT * FROM crawl_session ORDER BY start_time DESC")
    List<CrawlSession> findAll();

    @Select("SELECT * FROM crawl_session WHERE id = #{id}")
    Optional<CrawlSession> findById(Long id);

    @Select("SELECT * FROM crawl_session WHERE task_id = #{taskId} ORDER BY start_time DESC")
    List<CrawlSession> findByTaskId(Long taskId);

    @Insert("INSERT INTO crawl_session(task_id, start_time, status) " +
            "VALUES(#{taskId}, #{startTime}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CrawlSession session);

    @Update("UPDATE crawl_session SET " +
            "end_time=#{endTime}, status=#{status}, " +
            "pages_crawled=#{pagesCrawled}, " +
            "articles_extracted=#{articlesExtracted}, " +
            "error_message=#{errorMessage} " +
            "WHERE id=#{id}")
    void update(CrawlSession session);

    @Delete("DELETE FROM crawl_session WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT * FROM crawl_session WHERE task_id=#{taskId} AND status='RUNNING' LIMIT 1")
    CrawlSession findRunningByTaskId(Long taskId);

    @Select("SELECT * FROM crawl_session WHERE task_id=#{taskId} " +
            "ORDER BY start_time DESC LIMIT 1")
    CrawlSession findLastByTaskId(Long taskId);
}
