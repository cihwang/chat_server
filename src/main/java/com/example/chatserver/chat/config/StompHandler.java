package com.example.chatserver.chat.config;

import com.example.chatserver.chat.service.ChatService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StompHandler implements ChannelInterceptor {
    // session 객체 생성 제거를 stomp에서 관리
    // 인증 작업 필요
    // 사용자 요청에서 토큰을 꺼내 검증

    @Value("${jwt.secretKey}")
    private String secretKey;

    private final ChatService chatService;

    @Autowired
    public StompHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * connect 하기전에, subscribe하기전에, disconnect하기 전에 처리하는 작업
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(StompCommand.CONNECT == accessor.getCommand()){
            log.info("connect 요청 시 토큰 유효성 검증");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);

            // token 검증 (Claims는 따로 사용 X)
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.info("토큰 검증 완료");
        }

        if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
            log.info("subscribe 검증");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);

            // token 검증 (Claims는 따로 사용 X)
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();
            // subscribe 요청시 보낸 url이 담김 -> roomId를 substring해서 가져오기
            Long roomId = Long.parseLong(accessor.getDestination().split("/")[2]);

            // 해당 사용자가 participant로 등록되어있는지 확인
            if(!chatService.isRoomParticipant(email, roomId)){
                throw new AuthenticationServiceException("해당 room에 권한이 없습니다.");
            }

            log.info("subscribe 검증 완료");
        }


        return message;
    }


}
