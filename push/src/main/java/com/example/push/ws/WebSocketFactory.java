package com.example.push.ws;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.push.constant.WsConstants;
import com.example.push.handler.BaseWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class WebSocketFactory {

    private final Map<String, BaseWebSocketHandler> handlers;

    /**
     * websocket连接,发送请求
     */
    public void process(WebSocketSession session, TextMessage message){
        String text = message.getPayload();
        try {
            JSONObject param = JSONUtil.parseObj(text);
            //订阅类型
            String subType = String.valueOf(param.get(WsConstants.subType));
            //取消订阅类型
            String unSubType = String.valueOf(param.get(WsConstants.unSubType));
            String sessionId = session.getId();

            //遍历所有handler
            handlers.forEach((key, handler) -> {
                if(ObjectUtil.isNotEmpty(subType) && key.equalsIgnoreCase(subType)){
                    //订阅
                    // TODO: 2022/9/23  new JSONObject(ObjectUtil.defaultIfNull(param.get(WSConstants.data), new HashMap<>())).putOnce("userId", channelHandlerContext.getPrincipal().getName())
                    handler.connect(sessionId, new JSONObject(param.get(WsConstants.data)));
                } else if (ObjectUtil.isNotEmpty(unSubType) && key.equalsIgnoreCase(unSubType)) {
                    //取消订阅
                    handler.disconnect(sessionId);
                }
            });
        }catch (Exception e){
            log.error("接收到的参数不为JSON数据类型:{}", text, e);
        }
    }

    /**
     * 执行定时任务, 通过websocket返回数据给前端
     */
    @Scheduled(cron = "0/1 * * * * ?")
    public void run() {
        handlers.values().forEach(handler -> {
            try {
                handler.task();
            } catch (Exception e) {
                log.error("定时执行websocket 数据异常", e);
            }
        });
    }


}
