package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.service.ChatService;
import com.example.chatserver.chat.service.RedisPubSubService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;
    private final RedisPubSubService redisPubSubService;

    @Autowired
    public StompController(SimpMessageSendingOperations messageTemplate, ChatService chatService, RedisPubSubService redisPubSubService) {
        this.messageTemplate = messageTemplate;
        this.chatService = chatService;
        this.redisPubSubService = redisPubSubService;
    }

    /**
     * 방법1) Message Mapping과 sendTo 한꺼번에 처리
     * 메시지 Broker 역할
     * @DestinationVariable : @MessageMapping으로 정의된 웹소켓 컨트롤러 내에서만 사용
     */
//    @MessageMapping("/{roomId}") // client에서 특정 /publish/roomId 형태로 메시지 발행시 수신
//    @SendTo("/topic/{roomId}") // 해당 roomId에 메시지를 발행하여 구독중인 client에게 메시지 전송
//    public String sendMessage(@DestinationVariable Long roomId, String message) {
//        log.info(message);
//        return message; // sendTo로 메시지 발행
//    }

    /**
     * 방법2) MessageMapping 어노테이션만 활용
     * sendTo는 직접 전달하는 방식으로구현(추후 Redis 활용을 위해)
     *
     * MessageMapping이 특정 STOMP 토픽에 발행되는 메시지를 받는다. ->
     *
     */
    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageDto) throws JsonProcessingException {
        log.info(chatMessageDto.getSenderEmail() + " : " + chatMessageDto.getMessage());

        // DB에 저장
        chatService.saveMessage(roomId, chatMessageDto);
        chatMessageDto.setRoomId(roomId);
//        messageTemplate.convertAndSend("/topic/" + roomId, chatMessageDto); // = sendTo

        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(chatMessageDto);

        // publish 호출
        redisPubSubService.publish("chat", message);
    }
}
