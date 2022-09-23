package com.example.push.ws;

import cn.hutool.json.JSON;

public interface WebSocketSessionStoreService {

    Boolean sendMessage(String sessionId, JSON json);
}
