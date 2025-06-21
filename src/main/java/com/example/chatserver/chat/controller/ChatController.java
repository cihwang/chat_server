package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.dto.ChatRoomListResDto;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.service.ChatService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    public ChatController(ChatService chatService, ChatRoomRepository chatRoomRepository) {
        this.chatService = chatService;
        this.chatRoomRepository = chatRoomRepository;
    }

    /**
     * 그룹 채팅방 개설
     */
    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom(@RequestParam String chatRoomName){
        chatService.createGroupRoom(chatRoomName);
        return ResponseEntity.ok().build();
    }

    // 그룹채팅 목록 조회
    @GetMapping("/room/group/list")
    public ResponseEntity<?> getGroupChatRooms(){
        List<ChatRoomListResDto> chatRooms = chatService.getGroupChatRooms();
        return new ResponseEntity<>(chatRooms, HttpStatus.OK);
    }

    //group 채팅방 참여
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<?> joinGroupChatRoom(@PathVariable Long roomId){
        chatService.addParticipantToGroupChat(roomId); // 내가 참여 -> Authentication 객체에서 꺼낼 것
        return ResponseEntity.ok().build();
    }
}
