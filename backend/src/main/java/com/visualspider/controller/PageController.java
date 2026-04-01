package com.visualspider.controller;

import com.visualspider.domain.Article;
import com.visualspider.domain.CrawlSession;
import com.visualspider.domain.CrawlTask;
import com.visualspider.repository.ArticleMapper;
import com.visualspider.repository.CrawlSessionMapper;
import com.visualspider.repository.CrawlTaskMapper;
import com.visualspider.repository.PageSnapshotMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/articles")
    public String articles(Model model) {
        List<Article> articles = articleMapper.findAll();
        model.addAttribute("articles", articles);
        return "articles/index";
    }

    @GetMapping("/sessions")
    public String sessions(Model model) {
        List<CrawlSession> sessions = crawlSessionMapper.findAll();
        model.addAttribute("sessions", sessions);
        return "sessions/index";
    }

    @GetMapping("/sessions/detail")
    public String sessionDetail(@RequestParam Long sessionId, Model model) {
        var sessionOpt = crawlSessionMapper.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return "redirect:/sessions";
        }
        model.addAttribute("session", sessionOpt.get());
        model.addAttribute("snapshots", pageSnapshotMapper.findBySessionId(sessionId));
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
}
