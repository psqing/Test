package com.example.push.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import com.example.push.ws.WebSocketSessionStoreService;

public interface BaseWebSocketHandler {

    /**
     * 连接websocket，
     * @param sessionId
     * @param json
     */
    void connect(String sessionId, JSONObject json);

    /**
     * 取消订阅
     * @param sessionId
     */
    void disconnect(String sessionId);

    /**
     * 定时任务定时发送数据给前端
     */
    void task();

    default boolean send(String sessionId, JSON json){
        WebSocketSessionStoreService sessionStoreService = SpringUtil.getBean(WebSocketSessionStoreService.class);
        return sessionStoreService.sendMessage(sessionId, json);
    }


}
