package org.clf.springboot.websocket;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.clf.springboot.entity.Msg;
import org.clf.springboot.mapper.MsgMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ai-picture/result/{userId}")
@Component
public class ImageResultWebSocket {

    private static final Logger logger = LoggerFactory.getLogger(ImageResultWebSocket.class);

    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @Resource
    private MsgMapper msgMapper;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        SESSION_MAP.put(userId, session);
        logger.info("WebSocket链接建立,userId={},sessionId={}", userId, session.getId());
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        SESSION_MAP.remove(userId);
        logger.info("WebSocket链接关闭,userId={},sessionId={}", userId, session.getId());
    }

    @OnError
    public void onError(Session session, @PathParam("userId") String userId, Throwable throwable) {
        logger.error("WebSocket发生错误,userId={},sessionId={}", userId, session.getId(), throwable);
    }

    public void sendMessage(String userId, Msg data) {
        Session session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(JSON.toJSONString(data));
                logger.info("发送信息成功,userId={},sessionId={}", userId, session.getId());
            } catch (Exception e) {
                logger.error("WebSocket推送消息失败", e);
            }
        } else {
            //TODO 离线发送
            msgMapper.insert(data);
            logger.error("WebSocket无有效链接, userId={}", userId);
        }
    }

}
