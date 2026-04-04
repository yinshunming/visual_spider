package com.visualspider.controller;

import com.visualspider.domain.PageSnapshot;
import com.visualspider.repository.PageSnapshotMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/snapshots")
public class PageSnapshotController {

    private final PageSnapshotMapper pageSnapshotMapper;

    public PageSnapshotController(PageSnapshotMapper pageSnapshotMapper) {
        this.pageSnapshotMapper = pageSnapshotMapper;
    }

    @GetMapping
    public List<PageSnapshot> list() {
        return pageSnapshotMapper.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PageSnapshot> get(@PathVariable Long id) {
        return pageSnapshotMapper.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/session/{sessionId}")
    public List<PageSnapshot> getBySessionId(@PathVariable Long sessionId) {
        return pageSnapshotMapper.findBySessionId(sessionId);
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody PageSnapshot snapshot) {
        snapshot.setCreatedAt(LocalDateTime.now());
        pageSnapshotMapper.insert(snapshot);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pageSnapshotMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
