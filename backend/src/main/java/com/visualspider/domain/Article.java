package com.visualspider.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Article 实体 - 抓取结果的目标表
 */
public class Article {
    private Long id;
    private String url;
    private String title;
    private String content;
    private String author;
    private LocalDate publishDate;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;

    public Article() {}

    public Article(Long id, String url, String title, String content, String author,
                   LocalDate publishDate, String source, LocalDateTime createdAt,
                   LocalDateTime updatedAt, String status) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.content = content;
        this.author = author;
        this.publishDate = publishDate;
        this.source = source;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    // Getters
    public Long getId() { return id; }
    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public LocalDate getPublishDate() { return publishDate; }
    public String getSource() { return source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getStatus() { return status; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUrl(String url) { this.url = url; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }
    public void setSource(String source) { this.source = source; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setStatus(String status) { this.status = status; }
}
