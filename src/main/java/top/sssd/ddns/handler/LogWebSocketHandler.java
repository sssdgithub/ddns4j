package top.sssd.ddns.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author sssd
 * @created 2023-05-05-12:58
 */
@Component
@Slf4j
public class LogWebSocketHandler extends TextWebSocketHandler {

    /**
     * 存储连接的WebSocket会话列表
     */
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 添加新的WebSocket会话
        sessions.add(session);
        log.info("WebSocket session {} connected", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除关闭的WebSocket会话
        sessions.remove(session);
        log.info("WebSocket session {} closed with status {}", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端发送的消息
        log.info("Received message '{}' from WebSocket session {}", message.getPayload(), session.getId());
    }

    /**
     * 发送消息到所有WebSocket会话
     * @param message
     */
    public void sendToAllSessions(String message) {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("Failed to send message to WebSocket session {}", session.getId(), e);
            }
        }
    }
}
