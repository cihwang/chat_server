package com.example.chatserver.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 핸들러로 등록되기 위해서는 TextWebSocketHandler(등록될 수 있는 객체)를 상속받아야함
 * /connect로 요청이 들어왔을때 요청을 처리할 핸들러
 * 메모리에 사용자 등록
 * 연결이 끊기면 사용자 목록에서 제외
 *
 */
@Slf4j
@Component
public class SimpleWebSocketHandler extends TextWebSocketHandler {

    // 연결된 세션 관리 - thread safe한 set 사용
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    /**
     * 연결이 되면?
     * 메모리에 사용자 등록
     * 세션 내 정보: 사용자 ip, 사용자 mac, browser 정보 등등
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("Connected: " + session.getId());
    }

    /**
     * 사용자에게 메시지를 보내주는 메서드
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received Message: " + payload);

        for(WebSocketSession s : sessions) {
            // 받을 수 있는 상태라면
            if(s.isOpen()){
                // 메시지를 보내겠다.
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    /**
     * 연결이 끊기면?
     * 사용자 목록에서 제거
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("Disconnected: " + session.getId());
    }

}
