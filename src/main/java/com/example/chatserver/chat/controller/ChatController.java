package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.dto.ChatRoomListResDto;
import com.example.chatserver.chat.dto.MyChatResListDto;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.service.ChatService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatController(ChatService chatService, ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository) {
        this.chatService = chatService;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
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

    // 이전 메시지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long roomId){
        List<ChatMessageDto> chatMessageDtos = chatService.getChatHistory(roomId);
        log.info(chatMessageDtos.toString());
        return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
    }

    // 채팅 메시지 읽음 처리
    // discount 시점에 ChatRoom에 존재하는 모든 message 읽음 처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> read(@PathVariable Long roomId){
        chatService.messageRead(roomId);
        return ResponseEntity.ok().build();
    }

    // 채팅방 목록 조회 roomId, roomName, isGroupChat(groupChat만 나가기 기능활성화), messageReadCnt
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyChatRooms(){
        List<MyChatResListDto> myChatResListDtos = chatService.getMyChatRooms();
        return new ResponseEntity<>(myChatResListDtos ,HttpStatus.OK);
    }

    // group 채팅방 나가기
    @DeleteMapping("/room/group/{roomId}/leave")
    public ResponseEntity<?> leaveGroupChatRoom(@PathVariable Long roomId) {
        chatService.leaveGroupChatRoom(roomId);
        return ResponseEntity.ok().build();
    }

    // 개인 채팅방 개설 or 기존 룸 아이디 return
    @PostMapping("/room/private/create")
    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam Long otherMemberId) {
        Long roomId = chatService.getOrCreatePrivateRoom(otherMemberId);
        return new ResponseEntity<>(roomId, HttpStatus.OK);
    }
}
