package org.clf.springboot.websocket;

import com.alibaba.fastjson.JSON;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ai-picture/result/{taskId}")
@Component
public class ImageResultWebSocket {

    private static final Logger logger = LoggerFactory.getLogger(ImageResultWebSocket.class);

    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("taskId") String taskId) {
        SESSION_MAP.put(taskId, session);
        logger.info("WebSocket链接建立,taskId={},sessionId={}", taskId, session.getId());
    }

    @OnClose
    public void onClose(Session session, @PathParam("taskId") String taskId) {
        SESSION_MAP.remove(taskId);
        logger.info("WebSocket链接关闭,taskId={},sessionId={}", taskId, session.getId());
    }

    @OnError
    public void onError(Session session, @PathParam("taskId") String taskId, Throwable throwable) {
        logger.error("WebSocket发生错误,taskId={},sessionId={}", taskId, session.getId(), throwable);
    }

    public static void sendMessage(String taskId, Object data) {
        Session session = SESSION_MAP.get(taskId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(JSON.toJSONString(data));
                logger.info("发送信息成功,taskId={},sessionId={}", taskId, session.getId());
            } catch (Exception e) {
                logger.error("WebSocket推送消息失败", e);
            }
        } else {
            logger.error("WebSocket无有效链接, taskId={}", taskId);
        }
    }
}
