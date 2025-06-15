package com.example.chatserver.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Autowired
    public StompWebSocketConfig(StompHandler stompHandler) {
        this.stompHandler = stompHandler;
    }

    /**
     * 엔드포인트 설정, CORS 설정, SockJS 설정
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setAllowedOrigins("http://localhost:5173")
//                ws:// 가 아니라 http:// 엔드포인트를 사용할 수 있게 해주는 sockJS 라이브러리를 통한 요청을 허용하는 설정
//                front에서 sockJS를 사용해서 요청할 것이기 때문
                .withSockJS();
    }

    /**
     * rommId별로 subscribe하고 publish 하도록 설정
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 발행할때 /publish/{roomNum} -> /publish로 시작해야 발행 가능
        // /publish로 시작하는 url 패턴으로 메시지가 발행되면 @Controller 객체의 @MessageMapping method로 전달된다.
        registry.setApplicationDestinationPrefixes("/publish");

        // /topic/{roomNum} 형태로 메시지를 수신해야함
        registry.enableSimpleBroker("/topic");
    }

    /**
     * 웹소켓 요청(connect, disconnect, subscribe) 등의 요청시에는 http header등 http 메시지를 넣어올 수 있고,
     * 이를 interceptor를 통해 가로채 token등을 검증할 수 있다.
     *
     * 사용자 connect 요청 -> Security Filter 통과 -> ''' interceptor로 가로채 token 검증 '''' -> StompWebSocketConfig에 들어와서 connect 맺기
     * front단에서 요청할때 localstorage에서 token을 가져와야할듯
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
