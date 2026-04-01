package com.visualspider.repository;

import com.visualspider.domain.CrawlTask;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CrawlTaskMapper {

    @Select("SELECT * FROM crawl_task WHERE id = #{id}")
    Optional<CrawlTask> findById(Long id);

    @Select("SELECT * FROM crawl_task ORDER BY created_at DESC")
    List<CrawlTask> findAll();

    @Select("SELECT * FROM crawl_task WHERE enabled = true")
    List<CrawlTask> findEnabled();

    @Select("SELECT * FROM crawl_task WHERE cron_expression IS NOT NULL AND cron_expression <> ''")
    List<CrawlTask> findAllWithCronExpression();

    @Insert("INSERT INTO crawl_task (name, seed_url, pagination_selector, pagination_type, detail_url_pattern, " +
            "max_pages, enabled, cron_expression, created_at, updated_at) " +
            "VALUES (#{name}, #{seedUrl}, #{paginationSelector}, #{paginationType}, #{detailUrlPattern}, " +
            "#{maxPages}, #{enabled}, #{cronExpression}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CrawlTask crawlTask);

    @Update("UPDATE crawl_task SET name=#{name}, seed_url=#{seedUrl}, pagination_selector=#{paginationSelector}, " +
            "pagination_type=#{paginationType}, detail_url_pattern=#{detailUrlPattern}, max_pages=#{maxPages}, " +
            "enabled=#{enabled}, cron_expression=#{cronExpression}, updated_at=#{updatedAt} WHERE id=#{id}")
    void update(CrawlTask crawlTask);

    @Delete("DELETE FROM crawl_task WHERE id = #{id}")
    void deleteById(Long id);
}
