package com.example.chatserver.chat.config;

import io.lettuce.core.event.connection.DisconnectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * spring과 stomp는 세션관리를 내부적으로 처리
 * 연결/해제 이벤트를 기록하고 연결된 세션 수를 실시간으로 확인할 목적으로 이벤틀 리스터를 생성
 * -> 로그, 디버깅 목적
 */
@Slf4j
@Component
public class StompEventListener {

    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    /**
     * event 객체 안에 사용자의 헤더 정보가 들어가있음
     * connect 시
     */
    @EventListener
    public void connectHandle(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.add(accessor.getSessionId());

        log.info("connect Session ID : " + accessor.getSessionId());
        log.info("total Sessions : " + sessions.size());
    }

    /**
     * event 객체 안에 사용자의 헤더 정보가 들어가있음
     * connect 시
     */
    @EventListener
    public void disconnectHandle(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.remove(accessor.getSessionId());

        log.info("disconnect Session ID : " + accessor.getSessionId());
        log.info("total Sessions : " + sessions.size());
    }
}
