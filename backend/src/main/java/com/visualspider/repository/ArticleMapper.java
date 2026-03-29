package com.visualspider.repository;

import com.visualspider.domain.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ArticleMapper {

    @Select("SELECT * FROM article WHERE id = #{id}")
    Optional<Article> findById(Long id);

    @Select("SELECT * FROM article ORDER BY created_at DESC")
    List<Article> findAll();

    @Select("SELECT * FROM article WHERE url = #{url}")
    Optional<Article> findByUrl(String url);

    @Insert("INSERT INTO article (url, title, content, author, publish_date, source, created_at, updated_at, status) " +
            "VALUES (#{url}, #{title}, #{content}, #{author}, #{publishDate}, #{source}, #{createdAt}, #{updatedAt}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Article article);

    @Update("UPDATE article SET url=#{url}, title=#{title}, content=#{content}, author=#{author}, " +
            "publish_date=#{publishDate}, source=#{source}, updated_at=#{updatedAt}, status=#{status} WHERE id=#{id}")
    void update(Article article);

    @Delete("DELETE FROM article WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) FROM article")
    long count();
}
