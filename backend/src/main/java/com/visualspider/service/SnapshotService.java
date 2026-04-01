package com.visualspider.service;

import com.microsoft.playwright.Page;
import com.visualspider.config.SnapshotProperties;
import com.visualspider.domain.PageSnapshot;
import com.visualspider.repository.PageSnapshotMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class SnapshotService {

    private static final Logger log = LoggerFactory.getLogger(SnapshotService.class);

    private final PageSnapshotMapper pageSnapshotMapper;
    private final SnapshotProperties snapshotProperties;

    public SnapshotService(PageSnapshotMapper pageSnapshotMapper, SnapshotProperties snapshotProperties) {
        this.pageSnapshotMapper = pageSnapshotMapper;
        this.snapshotProperties = snapshotProperties;
    }

    public void saveSnapshot(Page page, Long sessionId, String url) {
        try {
            String sessionDir = snapshotProperties.getStoragePath() + "/" + sessionId + "/";
            Files.createDirectories(Paths.get(sessionDir));

            long timestamp = Instant.now().toEpochMilli();
            int urlHash = url.hashCode();
            String baseName = timestamp + "_" + urlHash;

            String htmlFileName = baseName + ".html";
            String pngFileName = baseName + ".png";
            String htmlPath = sessionDir + htmlFileName;
            String screenshotPath = sessionDir + pngFileName;

            Files.writeString(Paths.get(htmlPath), page.content());
            Files.write(Paths.get(screenshotPath), page.screenshot(new Page.ScreenshotOptions().setFullPage(true)));

            PageSnapshot snapshot = new PageSnapshot();
            snapshot.setSessionId(sessionId);
            snapshot.setUrl(url);
            snapshot.setHtmlPath(htmlPath);
            snapshot.setScreenshotPath(screenshotPath);
            snapshot.setCreatedAt(LocalDateTime.now());
            pageSnapshotMapper.insert(snapshot);

            log.info("快照已保存 sessionId={}, url={}", sessionId, url);
        } catch (Exception e) {
            log.warn("保存快照失败 sessionId={}, url={}, error={}", sessionId, url, e.getMessage());
        }
    }
}
