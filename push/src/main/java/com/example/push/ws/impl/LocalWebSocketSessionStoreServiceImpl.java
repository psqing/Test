package com.example.push.ws.impl;

import cn.hutool.cache.Cache;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.example.push.ws.WebSocketSessionStoreService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class LocalWebSocketSessionStoreServiceImpl implements WebSocketSessionStoreService {

    /**
     * Key: sessionId
     * value: websocketSession
     */
    private static final Cache<String, WebSocketSession> managerSessionStore = new TimedCache<>(1000*60*60*8); // 缓存时间1小时

    @Override
    @SneakyThrows
    public Boolean sendMessage(String sessionId, JSON json) {
        WebSocketSession webSocketSession = managerSessionStore.get(sessionId, false);
        log.debug("sessionId: {},连接:{} 给用户推送数据: {}", sessionId, webSocketSession, json);
        if (ObjectUtil.isNotEmpty(webSocketSession)) {
            TextMessage textMessage = new TextMessage(JSONUtil.toJsonStr(json));
            webSocketSession.sendMessage(textMessage);
        } else {
            log.error("当前 sessionId: {} 连接为空: {}", sessionId, webSocketSession);
        }
        return true;
    }
}
