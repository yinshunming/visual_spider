package com.visualspider.controller;

import com.visualspider.domain.Article;
import com.visualspider.domain.CrawlSession;
import com.visualspider.domain.CrawlTask;
import com.visualspider.repository.ArticleMapper;
import com.visualspider.repository.CrawlSessionMapper;
import com.visualspider.repository.CrawlTaskMapper;
import com.visualspider.repository.PageSnapshotMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class PageController {

    private final CrawlTaskMapper crawlTaskMapper;
    private final ArticleMapper articleMapper;
    private final CrawlSessionMapper crawlSessionMapper;
    private final PageSnapshotMapper pageSnapshotMapper;

    public PageController(CrawlTaskMapper crawlTaskMapper, ArticleMapper articleMapper,
                          CrawlSessionMapper crawlSessionMapper, PageSnapshotMapper pageSnapshotMapper) {
        this.crawlTaskMapper = crawlTaskMapper;
        this.articleMapper = articleMapper;
        this.crawlSessionMapper = crawlSessionMapper;
        this.pageSnapshotMapper = pageSnapshotMapper;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/tasks")
    public String tasks(Model model) {
        List<CrawlTask> tasks = crawlTaskMapper.findAll();
        model.addAttribute("tasks", tasks);
        return "tasks/index";
    }

    @GetMapping("/tasks/new")
    public String newTask() {
        return "tasks/new";
    }

    @GetMapping("/tasks/{id}/run")
    public String runTask(@PathVariable Long id) {
        return "redirect:/editor/" + id;
    }

    @GetMapping("/tasks/{id}")
    public String taskDetail(@PathVariable Long id, Model model) {
        var taskOpt = crawlTaskMapper.findById(id);
        if (taskOpt.isEmpty()) {
            return "redirect:/tasks";
        }
        model.addAttribute("task", taskOpt.get());
        model.addAttribute("sessions", crawlSessionMapper.findByTaskId(id));
        return "tasks/detail";
    }

    @GetMapping("/articles")
    public String articles(Model model) {
        List<Article> articles = articleMapper.findAll();
        model.addAttribute("articles", articles);
        return "articles/index";
    }

    @GetMapping("/sessions")
    public String sessions(Model model) {
        List<CrawlSession> crawlSessions = crawlSessionMapper.findAll();
        model.addAttribute("crawlSessions", crawlSessions);
        return "sessions/index";
    }

    @GetMapping("/sessions/detail")
    public String sessionDetail(@RequestParam Long sessionId, Model model) {
        return sessionDetailPath(sessionId, model);
    }

    @GetMapping("/sessions/{id}")
    public String sessionDetailPath(@PathVariable Long id, Model model) {
        var sessionOpt = crawlSessionMapper.findById(id);
        if (sessionOpt.isEmpty()) {
            return "redirect:/sessions";
        }
        model.addAttribute("session", sessionOpt.get());
        model.addAttribute("snapshots", pageSnapshotMapper.findBySessionId(id));
        return "sessions/detail";
    }

    @GetMapping("/editor")
    public String editor(Model model) {
        return "editor";
    }

    @GetMapping("/editor/{taskId}")
    public String editorWithTask(@PathVariable Long taskId, Model model) {
        CrawlTask task = crawlTaskMapper.findById(taskId).orElse(null);
        model.addAttribute("task", task);
        return "editor";
    }

    /**
     * 提供快照文件（HTML/PNG）访问。
     */
    @GetMapping("/sessions/files/{sessionId}/{filename:.+}")
    public ResponseEntity<byte[]> serveSnapshotFile(@PathVariable String sessionId, @PathVariable String filename) {
        try {
            Path baseDir = Paths.get("D:/opencodeSpace/visual_spider/backend/snapshots").toAbsolutePath();
            Path filePath = baseDir.resolve(sessionId).resolve(filename).normalize();
            java.io.File file = filePath.toFile();
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            byte[] data = Files.readAllBytes(filePath);
            String contentType = filename.endsWith(".png") ? "image/png" : "text/html";
            return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error: " + e.getClass().getName() + ": " + e.getMessage()).getBytes());
        }
    }
}
