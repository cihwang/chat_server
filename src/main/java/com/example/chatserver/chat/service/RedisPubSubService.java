package com.example.chatserver.chat.service;

import com.example.chatserver.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

/**
 * 메시지 Sub 객체이기 때문에 MessageListener 구현 필요
 */
@Service
public class RedisPubSubService implements MessageListener {

    // 발행객체
    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;

    @Autowired
    public RedisPubSubService(@Qualifier("chatPubSub") StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messageTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageTemplate = messageTemplate;
    }

    /**
     * 메시지 발행 -> 발행 객체 필요
     * 메시지 보내는 쪽(Redis로 쏘는 쪽) = STOMP Contoller
     */
    public void publish(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }

    /**
     * 메시지를 받은 다음 STOMP Topic에 메시지를 넣어줘야한다.
     * @param message 실질적으로 받아오는 메시지
     * @param pattern topic 이름의 패턴이 담김, 패턴을 기반으로 다양한 코딩 가능 -> 활용X
     *                MessageListenerAdapter에서 "chat"이라는 이름으로 수신 중
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {

        String payload = new String(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        ChatMessageDto chatMessageDto = null;
        try {
            chatMessageDto = objectMapper.readValue(payload, ChatMessageDto.class);
            // chatMessage에는 roomId가 없다. -> publish 전에 꽂아줘야한다.
            messageTemplate.convertAndSend("/topic/" + chatMessageDto.getRoomId(), chatMessageDto); // = sendTo

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
