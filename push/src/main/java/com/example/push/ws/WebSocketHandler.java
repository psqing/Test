package com.example.push.ws;

import com.example.common.util.SpringBeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class WebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionStoreService  webSocketSessionStoreService;

    /**
     * 用于记录和管理所有客户端的channel
     */
    private static final Map<String, WebSocketSession> channels = new ConcurrentHashMap<>(1000);

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        super.handleTextMessage(session, message);
        SpringBeanUtil.getBean(WebSocketFactory.class).process(session, message);
    }


}
