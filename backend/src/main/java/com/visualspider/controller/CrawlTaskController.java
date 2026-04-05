package com.visualspider.controller;

import com.visualspider.domain.CrawlTask;
import com.visualspider.repository.CrawlTaskMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class CrawlTaskController {

    private final CrawlTaskMapper crawlTaskMapper;

    public CrawlTaskController(CrawlTaskMapper crawlTaskMapper) {
        this.crawlTaskMapper = crawlTaskMapper;
    }

    @GetMapping
    public List<CrawlTask> list() {
        return crawlTaskMapper.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrawlTask> get(@PathVariable Long id) {
        return crawlTaskMapper.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody CrawlTask task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        if (task.getEnabled() == null) {
            task.setEnabled(false);
        }
        if (task.getMaxPages() == null) {
            task.setMaxPages(10);
        }
        crawlTaskMapper.insert(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(task.getId());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody CrawlTask task) {
        return crawlTaskMapper.findById(id)
                .map(existing -> {
                    task.setId(id);
                    task.setUpdatedAt(LocalDateTime.now());
                    crawlTaskMapper.update(task);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        crawlTaskMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 仅切换任务启用状态
     * PATCH /api/tasks/{id}/enabled
     * enabled=true → 已启用（不可再改为 disabled）
     * enabled=false → 已禁用（可改为 enabled）
     */
    @PatchMapping("/{id}/enabled")
    public ResponseEntity<Void> updateEnabled(@PathVariable Long id, @RequestBody java.util.Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().build();
        }
        return crawlTaskMapper.findById(id)
                .map(existing -> {
                    existing.setEnabled(enabled);
                    existing.setUpdatedAt(LocalDateTime.now());
                    crawlTaskMapper.update(existing);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
