package com.example.chatserver.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 메시지 핸들러기때문에 MessageListener 구현 필요
 */
@Service
public class RedisPubSubService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisPubSubService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * STOMP
     * @param message 실질적으로 받아오는 메시지
     * @param pattern topic 이름의 패턴이 담김, 패턴을 기반으로 다양한 코딩 가능 -> 활용X
     *                MessageListenerAdapter에서 "chat"이라는 이름으로 발행 중
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {

    }

    /**
     * 메시지 발행 -> 발행 객체 필요
     */
    public void publish(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }
}
