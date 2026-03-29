package com.visualspider.controller;

import com.visualspider.domain.CrawlSession;
import com.visualspider.repository.CrawlSessionMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class CrawlSessionController {

    private final CrawlSessionMapper crawlSessionMapper;

    public CrawlSessionController(CrawlSessionMapper crawlSessionMapper) {
        this.crawlSessionMapper = crawlSessionMapper;
    }

    @GetMapping
    public List<CrawlSession> list() {
        return crawlSessionMapper.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrawlSession> get(@PathVariable Long id) {
        return crawlSessionMapper.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/task/{taskId}")
    public List<CrawlSession> getByTaskId(@PathVariable Long taskId) {
        return crawlSessionMapper.findByTaskId(taskId);
    }

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody CrawlSession session) {
        session.setStartTime(LocalDateTime.now());
        if (session.getStatus() == null) {
            session.setStatus("RUNNING");
        }
        if (session.getPagesCrawled() == null) {
            session.setPagesCrawled(0);
        }
        if (session.getArticlesExtracted() == null) {
            session.setArticlesExtracted(0);
        }
        crawlSessionMapper.insert(session);
        return ResponseEntity.ok(session.getId());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody CrawlSession session) {
        return crawlSessionMapper.findById(id)
                .map(existing -> {
                    session.setId(id);
                    crawlSessionMapper.update(session);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        crawlSessionMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
