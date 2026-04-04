package com.visualspider.controller;

import com.visualspider.domain.Article;
import com.visualspider.repository.ArticleMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleMapper articleMapper;

    public ArticleController(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @GetMapping
    public List<Article> list() {
        return articleMapper.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> get(@PathVariable Long id) {
        return articleMapper.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody Article article) {
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        if (article.getStatus() == null) {
            article.setStatus("pending");
        }
        articleMapper.insert(article);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Article article) {
        return articleMapper.findById(id)
                .map(existing -> {
                    article.setId(id);
                    article.setUpdatedAt(LocalDateTime.now());
                    articleMapper.update(article);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        articleMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
