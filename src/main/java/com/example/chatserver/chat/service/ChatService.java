package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageReqDto;
import com.example.chatserver.chat.dto.ChatRoomListResDto;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatParticipantRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.repository.ReadStatusRepository;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public ChatService(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, ReadStatusRepository readStatusRepository, MemberRepository memberRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.readStatusRepository = readStatusRepository;
        this.memberRepository = memberRepository;
    }

    public void saveMessage(Long roomId, ChatMessageReqDto chatMessageReqDto) {
        //  채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room can not be found"));

        // 보낸 사람 조회
        Member sender = memberRepository.findByEmail(chatMessageReqDto.getSenderEmail()).orElseThrow(()-> new EntityNotFoundException("member can not be found"));

        // 메시지 저장(chatMessage를 저장하기위해 chatRoom과 ChatParticipant 필요
        ChatMessage chatmessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageReqDto.getMessage())
                .build();

        chatMessageRepository.save(chatmessage);

        // 사용자별로 읽음 여부 저장
        // 참여자 List 조회 필요(발송자 제외 나머지는 모두 안읽음 처리)
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);

        for (ChatParticipant c : participants) {
            // 참여자들을 대상으로 ReadStatus 저장 필요
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(c.getMember())
                    .chatMessage(chatmessage)
                    .isRead(c.getMember().equals(sender)? true : false)
                    .build();

            readStatusRepository.save(readStatus);
        }
    }

    public void createGroupRoom(String chatRoomName){

        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member can not be found"));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(chatRoomName)
                .isGroupChat("Y")
                .build();

        chatRoomRepository.save(chatRoom);

        // 채팅 참여자로 추가
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();

        chatParticipantRepository.save(chatParticipant);
    }

    /**
     * group 채팅 리스트 return
     * @return
     */
    public List<ChatRoomListResDto> getGroupChatRooms(){
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y");
        List<ChatRoomListResDto> dtos = new ArrayList<>();

        for (ChatRoom c : chatRooms) {
            ChatRoomListResDto dto = ChatRoomListResDto.builder()
                    .roomId(c.getId())
                    .roomName(c.getName())
                    .build();

            dtos.add(dto);
        }

        return dtos;
    }

    /**
     * room id에 대한 채팅방 조회 -> 내가 누군지 조회 -> participant에 '나' 추가
     */
    public void addParticipantToGroupChat(Long roomId){
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room can not be found"));

        // 멤버 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member can not be found"));

        // 이미 참여자인지 검증
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member);

        if(!participant.isPresent()){
            addParticipantToRoom(chatRoom, member);
        }
    }

    // 채팅방(그룹, 개인)에 참여자 추가
    public void addParticipantToRoom(ChatRoom chatRoom, Member member) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();

        chatParticipantRepository.save(chatParticipant);
    }
}
