package com.visualspider.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.visualspider.domain.SelectorSession;
import com.visualspider.dto.NodeSelection;
import com.visualspider.dto.SelectorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Selector 服务 - 管理浏览器会话和选择器
 */
@Service
public class SelectorService {
    private static final Logger log = LoggerFactory.getLogger(SelectorService.class);

    private final Browser browser;
    private final Map<String, SelectorSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // 注入的 JavaScript 代码
    private static final String INJECTED_JS = """
        (function() {
            document.addEventListener('click', async function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                var el = e.target;
                var candidates = [];
                
                // ID selector
                if (el.id) {
                    candidates.push({selector: '#' + el.id, selectorType: 'CSS', description: 'ID选择器'});
                }
                // Class selector
                if (el.className && typeof el.className === 'string') {
                    var cls = el.className.split(' ')[0];
                    if (cls) {
                        candidates.push({selector: el.tagName.toLowerCase() + '.' + cls, selectorType: 'CSS', description: 'class选择器'});
                    }
                }
                // Tag selector
                candidates.push({selector: el.tagName.toLowerCase(), selectorType: 'CSS', description: '标签选择器'});
                // Data attribute
                for (var i = 0; i < el.attributes.length; i++) {
                    var attr = el.attributes[i];
                    if (attr.name.startsWith('data-')) {
                        candidates.push({selector: '[' + attr.name + '="' + attr.value + '"]', selectorType: 'CSS', description: 'data-*属性'});
                        break;
                    }
                }
                // XPath (simple)
                var path = [];
                var current = el;
                while (current && current !== document.body) {
                    var xpath = current.tagName.toLowerCase();
                    if (current.id) {
                        xpath += '[@id="' + current.id + '"]';
                        path.unshift(xpath);
                        break;
                    } else if (current.className && typeof current.className === 'string') {
                        var cls = current.className.split(' ')[0];
                        if (cls) xpath += '[@class="' + cls + '"]';
                    }
                    path.unshift(xpath);
                    current = current.parentElement;
                }
                candidates.push({selector: '/' + path.join('/'), selectorType: 'XPATH', description: 'XPath'});
                
                // Send to backend
                var sessionId = window.SELECTOR_SESSION_ID;
                if (sessionId) {
                    var data = {
                        tagName: el.tagName,
                        id: el.id || '',
                        className: el.className || '',
                        textContent: el.textContent ? el.textContent.substring(0, 100) : '',
                        attributes: {},
                        candidates: candidates
                    };
                    
                    for (var j = 0; j < el.attributes.length; j++) {
                        var a = el.attributes[j];
                        data.attributes[a.name] = a.value;
                    }
                    
                    await fetch('/api/selector/session/' + sessionId + '/select', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify(data)
                    });
                    
                    // Highlight
                    el.style.outline = '3px solid red';
                }
            }, true);
        })();
        """;

    public SelectorService(Browser browser) {
        this.browser = browser;
    }

    /**
     * 启动新的选择会话
     */
    public String startSession(String url) throws Exception {
        // 创建新页面
        Page page = browser.newPage();
        
        // 创建 session
        SelectorSession session = new SelectorSession(url, page);
        sessions.put(session.getId(), session);
        
        // 创建 SSE emitters 列表
        emitters.put(session.getId(), new CopyOnWriteArrayList<>());
        
        // 设置 session ID 到 window 对象
        page.addInitScript("window.SELECTOR_SESSION_ID = '" + session.getId() + "';");
        
        // 注入选择器生成 JS
        page.addInitScript(INJECTED_JS);
        
        // 导航到 URL
        page.navigate(url);
        
        log.info("start_session id={} url={}", session.getId(), url);
        return session.getId();
    }

    /**
     * 添加选择到会话
     */
    public void addSelection(String sessionId, NodeSelection selection) {
        SelectorSession session = sessions.get(sessionId);
        if (session != null) {
            session.addSelection(selection);
            broadcast(sessionId, selection);
            log.info("add_selection sessionId={} tagName={}", sessionId, selection.getTagName());
        }
    }

    /**
     * 确认选择（设置字段名、提取类型等）
     */
    public void confirmSelection(String sessionId, int index, String fieldName, 
                                 String extractionType, String attributeName) {
        SelectorSession session = sessions.get(sessionId);
        if (session != null && index >= 0 && index < session.getSelections().size()) {
            NodeSelection selection = session.getSelections().get(index);
            selection.setFieldName(fieldName);
            selection.setExtractionType(extractionType);
            selection.setAttributeName(attributeName);
            
            // 默认选择第一个候选
            if (selection.getCandidates() != null && !selection.getCandidates().isEmpty()) {
                selection.setSelectedSelector(selection.getCandidates().get(0).getSelector());
                selection.setSelectorType(selection.getCandidates().get(0).getSelectorType());
            }
            
            broadcast(sessionId, selection);
            log.info("confirm_selection sessionId={} index={} fieldName={}", sessionId, index, fieldName);
        }
    }

    /**
     * 完成会话，返回结果并关闭浏览器
     */
    public SelectorResult completeSession(String sessionId) {
        SelectorSession session = sessions.remove(sessionId);
        if (session != null) {
            // 关闭浏览器页面
            if (session.getPage() != null) {
                ((Page) session.getPage()).close();
            }
            // 清理 emitters
            emitters.remove(sessionId);
            
            log.info("complete_session sessionId={} selections={}", sessionId, session.getSelections().size());
            
            return new SelectorResult(session.getId(), session.getUrl(), session.getSelections());
        }
        return null;
    }

    /**
     * 关闭会话（不返回结果）
     */
    public void closeSession(String sessionId) {
        SelectorSession session = sessions.remove(sessionId);
        if (session != null && session.getPage() != null) {
            ((Page) session.getPage()).close();
        }
        emitters.remove(sessionId);
        log.info("close_session sessionId={}", sessionId);
    }

    /**
     * 获取会话
     */
    public SelectorSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * 注册 SSE 连接
     */
    public SseEmitter createEmitter(String sessionId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        List<SseEmitter> sessionEmitters = emitters.get(sessionId);
        if (sessionEmitters != null) {
            sessionEmitters.add(emitter);
        }
        
        emitter.onCompletion(() -> {
            if (sessionEmitters != null) {
                sessionEmitters.remove(emitter);
            }
        });
        emitter.onTimeout(() -> {
            if (sessionEmitters != null) {
                sessionEmitters.remove(emitter);
            }
        });
        
        return emitter;
    }

    /**
     * 广播选择到所有 SSE 客户端
     */
    private void broadcast(String sessionId, NodeSelection selection) {
        List<SseEmitter> sessionEmitters = emitters.get(sessionId);
        if (sessionEmitters != null) {
            for (SseEmitter emitter : sessionEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("selection")
                        .data(selection));
                } catch (IOException e) {
                    emitter.complete();
                    sessionEmitters.remove(emitter);
                }
            }
        }
    }
}