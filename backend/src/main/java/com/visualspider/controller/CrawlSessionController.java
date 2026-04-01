package com.visualspider.controller;

import com.visualspider.domain.CrawlSession;
import com.visualspider.domain.PageSnapshot;
import com.visualspider.repository.CrawlSessionMapper;
import com.visualspider.repository.PageSnapshotMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class CrawlSessionController {

    private final CrawlSessionMapper crawlSessionMapper;
    private final PageSnapshotMapper pageSnapshotMapper;

    public CrawlSessionController(CrawlSessionMapper crawlSessionMapper, PageSnapshotMapper pageSnapshotMapper) {
        this.crawlSessionMapper = crawlSessionMapper;
        this.pageSnapshotMapper = pageSnapshotMapper;
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

    // ─── Thymeleaf endpoints ────────────────────────────────────────────────

    @GetMapping("/sessions")
    public String index(Model model) {
        model.addAttribute("sessions", crawlSessionMapper.findAll());
        return "sessions/index";
    }

    @GetMapping("/sessions/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var sessionOpt = crawlSessionMapper.findById(id);
        if (sessionOpt.isEmpty()) {
            return "redirect:/sessions";
        }
        model.addAttribute("session", sessionOpt.get());
        List<PageSnapshot> snapshots = pageSnapshotMapper.findBySessionId(id);
        model.addAttribute("snapshots", snapshots);
        return "sessions/detail";
    }

    @GetMapping("/sessions/files/**")
    public ResponseEntity<Resource> serveSnapshot(HttpServletRequest request) {
        String path = request.getRequestURI().replace("/sessions/files/", "");
        try {
            Resource resource = new org.springframework.core.io.FileSystemResource(path);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = path.endsWith(".png") ? "image/png" : "text/html";
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
