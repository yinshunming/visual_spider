package com.visualspider.repository;

import com.visualspider.domain.CrawlSession;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CrawlSessionMapper {

    @Select("SELECT * FROM crawl_session WHERE id = #{id}")
    Optional<CrawlSession> findById(Long id);

    @Select("SELECT * FROM crawl_session WHERE task_id = #{taskId} ORDER BY start_time DESC")
    List<CrawlSession> findByTaskId(Long taskId);

    @Select("SELECT * FROM crawl_session ORDER BY start_time DESC")
    List<CrawlSession> findAll();

    @Insert("INSERT INTO crawl_session (task_id, start_time, end_time, status, pages_crawled, articles_extracted, error_message) " +
            "VALUES (#{taskId}, #{startTime}, #{endTime}, #{status}, #{pagesCrawled}, #{articlesExtracted}, #{errorMessage})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CrawlSession crawlSession);

    @Update("UPDATE crawl_session SET task_id=#{taskId}, start_time=#{startTime}, end_time=#{endTime}, " +
            "status=#{status}, pages_crawled=#{pagesCrawled}, articles_extracted=#{articlesExtracted}, " +
            "error_message=#{errorMessage} WHERE id=#{id}")
    void update(CrawlSession crawlSession);

    @Delete("DELETE FROM crawl_session WHERE id = #{id}")
    void deleteById(Long id);
}
