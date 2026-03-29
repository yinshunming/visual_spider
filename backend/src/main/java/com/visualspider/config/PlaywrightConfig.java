package com.visualspider.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Playwright 配置类
 * 使用独立浏览器进程模式
 */
@Configuration
public class PlaywrightConfig {

    @Bean
    @Primary
    public Playwright playwright() {
        return Playwright.create();
    }

    @Bean
    @Primary
    public Browser browser(Playwright playwright) {
        // 使用 Chromium 作为默认浏览器
        // headless 模式下无法截图，所以这里用非 headless
        // 实际使用时通过前端控制是否显示浏览器窗口
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(java.util.List.of("--start-maximized")));
    }
}
