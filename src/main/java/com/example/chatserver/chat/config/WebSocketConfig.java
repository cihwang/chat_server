/*
package com.example.chatserver.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// 웹소켓 관련 설정
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SimpleWebSocketHandler simpleWebSocketHandler;

    @Autowired
    public WebSocketConfig(SimpleWebSocketHandler simpleWebSocketHandler) {
        this.simpleWebSocketHandler = simpleWebSocketHandler;
    }

    */
/**
     * webSocket 소스 코드를 처리할 웹소켓 핸들러를 등록해라
     * @param registry
     *//*

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
       // /connect url로 웹소켓 연결 요청이 들어오면 핸들러 클래스가 처리
        registry.addHandler(simpleWebSocketHandler,"/connect")
                // springSecurityConfig에서 cors 예외는 http 요청에 대한 예외.
                // 따라서 websocket 프로토콜에 대한 요청에는 별도의 cors 설정이 필요
                .setAllowedOrigins("http://localhost:5173");
    }
}
*/
