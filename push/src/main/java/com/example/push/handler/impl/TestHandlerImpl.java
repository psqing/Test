package com.example.push.handler.impl;

import cn.hutool.cache.Cache;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.push.constant.WsConstants;
import com.example.push.handler.BaseWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service(WsConstants.TEST_SUB)
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class TestHandlerImpl implements BaseWebSocketHandler {

    /**
     * key: sessionId
     * value: 参数
     * 保存websocket发送过来的信息
     */
    private static final Cache<String, JSONObject> concurrentMap = new TimedCache<>(1000 * 60 * 60 * 24); //   缓存一天

    /**
     * 推送消息
     * @param sessionId
     * @param jsonObject
     */
    private void run(String sessionId, JSONObject jsonObject){
        this.send(sessionId, JSONUtil.parse(jsonObject));
    }

    @Override
    public void connect(String sessionId, JSONObject json) {
        log.info("ws连接，SessionId:{}", sessionId);
        concurrentMap.put(sessionId, json);
        //订阅后，立即发送一次消息
        run(sessionId, json);
    }

    @Override
    public void disconnect(String sessionId) {
        log.info("ws断开连接，SessionId:{}", sessionId);
        concurrentMap.remove(sessionId);
    }

    @Override
    public void task() {
        //定时请求
        concurrentMap.cacheObjIterator().forEachRemaining(v ->{
            run(v.getKey(), v.getValue());
        });
    }

}
