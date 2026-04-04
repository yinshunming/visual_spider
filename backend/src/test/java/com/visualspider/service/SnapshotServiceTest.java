package com.visualspider.service;

import com.microsoft.playwright.Page;
import com.visualspider.config.SnapshotProperties;
import com.visualspider.domain.PageSnapshot;
import com.visualspider.repository.PageSnapshotMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SnapshotService - 页面快照服务")
class SnapshotServiceTest {

    @Mock
    private PageSnapshotMapper pageSnapshotMapper;

    private SnapshotProperties snapshotProperties;

    private SnapshotService snapshotService;

    private static Path tempBaseDir;

    @BeforeAll
    static void beforeAll() throws IOException {
        tempBaseDir = Files.createTempDirectory("snapshot-test");
    }

    @AfterAll
    static void afterAll() throws IOException {
        if (tempBaseDir != null) {
            deleteDirectoryRecursively(tempBaseDir);
        }
    }

    private static void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            try (var stream = Files.walk(path)) {
                stream.sorted((a, b) -> b.compareTo(a))
                      .forEach(p -> {
                          try { Files.delete(p); } catch (IOException ignored) {}
                      });
            }
        }
    }

    @BeforeEach
    void setUp() {
        snapshotProperties = new SnapshotProperties();
        snapshotProperties.setStoragePath(tempBaseDir.toString());
        snapshotService = new SnapshotService(pageSnapshotMapper, snapshotProperties);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up session directories after each test
        Path sessionDir = Paths.get(snapshotProperties.getStoragePath(), "1");
        if (Files.exists(sessionDir)) {
            deleteDirectoryRecursively(sessionDir);
        }
        Path sessionDir2 = Paths.get(snapshotProperties.getStoragePath(), "2");
        if (Files.exists(sessionDir2)) {
            deleteDirectoryRecursively(sessionDir2);
        }
    }

    // ==================== N1: saveSnapshot() - normal flow ====================

    @Nested
    @DisplayName("N1: saveSnapshot() - normal flow, files written and mapper.insert() called")
    class N1_NormalFlow {

        @Test
        @DisplayName("N1: normal flow — files written and mapper.insert() called with correct fields")
        void saveSnapshot_normalFlow_filesWrittenAndMapperCalled() throws Exception {
            Page page = mock(Page.class);
            when(page.content()).thenReturn("<html>test</html>");
            when(page.screenshot(any(Page.ScreenshotOptions.class))).thenReturn(new byte[0]);

            snapshotService.saveSnapshot(page, 1L, "https://example.com/article/1");

            // Verify files exist
            Path sessionDir = Paths.get(snapshotProperties.getStoragePath(), "1");
            assertThat(Files.exists(sessionDir)).isTrue();

            // Capture the snapshot passed to mapper
            ArgumentCaptor<PageSnapshot> captor = ArgumentCaptor.forClass(PageSnapshot.class);
            verify(pageSnapshotMapper, times(1)).insert(captor.capture());

            PageSnapshot captured = captor.getValue();
            assertThat(captured.getSessionId()).isEqualTo(1L);
            assertThat(captured.getUrl()).isEqualTo("https://example.com/article/1");
            assertThat(captured.getHtmlPath()).endsWith(".html");
            assertThat(captured.getScreenshotPath()).endsWith(".png");

            // Verify files actually exist on disk
            assertThat(Files.exists(Paths.get(captured.getHtmlPath()))).isTrue();
            assertThat(Files.exists(Paths.get(captured.getScreenshotPath()))).isTrue();
        }
    }

    // ==================== N2: saveSnapshot() - directory creation fails ====================

    @Nested
    @DisplayName("N2: saveSnapshot() - swallowed exception when directory creation fails")
    class N2_DirectoryCreationFails {

        @Test
        @DisplayName("N2: directory creation fails — exception swallowed, no mapper call")
        void saveSnapshot_dirCreationFails_exceptionSwallowedNoMapperCall() {
            // Use a path that cannot be created on Windows
            SnapshotProperties invalidProps = new SnapshotProperties();
            invalidProps.setStoragePath("/proc/invalid/path");
            SnapshotService serviceWithInvalidPath = new SnapshotService(pageSnapshotMapper, invalidProps);

            Page page = mock(Page.class);

            // Should NOT throw
            Assertions.assertDoesNotThrow(() ->
                serviceWithInvalidPath.saveSnapshot(page, 1L, "https://example.com")
            );

            // mapper.insert() should NOT be called because the save fails silently
            verify(pageSnapshotMapper, never()).insert(any());
        }
    }

    // ==================== N3: saveSnapshot() - page.content() throws ====================

    @Nested
    @DisplayName("N3: saveSnapshot() - page throws on content()")
    class N3_ContentThrows {

        @Test
        @DisplayName("N3: page.content() throws — exception swallowed, no mapper call")
        void saveSnapshot_contentThrows_exceptionSwallowedNoMapperCall() {
            Page page = mock(Page.class);
            when(page.content()).thenThrow(new RuntimeException("network error"));

            // Should NOT throw
            Assertions.assertDoesNotThrow(() ->
                snapshotService.saveSnapshot(page, 1L, "https://example.com")
            );

            // mapper.insert() should NOT be called
            verify(pageSnapshotMapper, never()).insert(any());
        }
    }

    // ==================== N4: saveSnapshot() - page.screenshot() throws ====================

    @Nested
    @DisplayName("N4: saveSnapshot() - page throws on screenshot()")
    class N4_ScreenshotThrows {

        @Test
        @DisplayName("N4: page.screenshot() throws after content() succeeds — exception swallowed, no mapper call")
        void saveSnapshot_screenshotThrows_exceptionSwallowedNoMapperCall() {
            Page page = mock(Page.class);
            when(page.content()).thenReturn("<html>ok</html>");
            when(page.screenshot(any(Page.ScreenshotOptions.class)))
                .thenThrow(new RuntimeException("screenshot failed"));

            // Should NOT throw
            Assertions.assertDoesNotThrow(() ->
                snapshotService.saveSnapshot(page, 1L, "https://example.com")
            );

            // mapper.insert() should NOT be called
            verify(pageSnapshotMapper, never()).insert(any());

            // HTML file should NOT be written because screenshot fails before we get to mapper.insert()
            // but after content() succeeds - however since we don't have a hook to verify HTML file
            // existence in this case, we just verify mapper was not called
        }
    }

    // ==================== N5: saveSnapshot() - mapper insert failure ====================

    @Nested
    @DisplayName("N5: saveSnapshot() - mapper insert failure does not propagate")
    class N5_MapperInsertFails {

        @Test
        @DisplayName("N5: mapper.insert() throws — exception caught and swallowed, no propagation")
        void saveSnapshot_mapperInsertFails_exceptionSwallowed() {
            Page page = mock(Page.class);
            when(page.content()).thenReturn("<html>test</html>");
            when(page.screenshot(any(Page.ScreenshotOptions.class))).thenReturn(new byte[0]);

            doThrow(new RuntimeException("DB error"))
                .when(pageSnapshotMapper).insert(any(PageSnapshot.class));

            // Should NOT throw — exception is caught and logged as warning
            Assertions.assertDoesNotThrow(() ->
                snapshotService.saveSnapshot(page, 1L, "https://example.com")
            );

            // Verify mapper was called
            verify(pageSnapshotMapper, times(1)).insert(any(PageSnapshot.class));
        }
    }
}
