package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.dto.ChatRoomListResDto;
import com.example.chatserver.chat.dto.MyChatResListDto;
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

    public void saveMessage(Long roomId, ChatMessageDto chatMessageDto) {
        //  채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room can not be found"));

        // 보낸 사람 조회
        Member sender = memberRepository.findByEmail(chatMessageDto.getSenderEmail()).orElseThrow(()-> new EntityNotFoundException("member can not be found"));

        // 메시지 저장(chatMessage를 저장하기위해 chatRoom과 ChatParticipant 필요
        ChatMessage chatmessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageDto.getMessage())
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

    // 채팅방 기록 가져오기
    public List<ChatMessageDto> getChatHistory(Long roomId){
        // 참여자인지 확인 필요
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room can not be found"));
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member can not be found"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean isParticipant = false;
        for(ChatParticipant c : chatParticipants){
            if(c.getMember().equals(member)){
                isParticipant = true;
                break;
            }
        }

        if(!isParticipant){
            throw new IllegalArgumentException("본인이 속하지 않은 채팅방입니다.");
        }

        // 특정 room에 대한 메시지 조회
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        List<ChatMessageDto> dtos = new ArrayList<>();
        for(ChatMessage c : chatMessages){
            ChatMessageDto dto = ChatMessageDto.builder()
                    .message(c.getContent())
                    .senderEmail(c.getMember().getEmail())
                    .build();

            dtos.add(dto);
        }

        return dtos;
    }

    // subscribe 검증
    public boolean isRoomParticipant(String email, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room can not be found"));
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member can not be found"));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);

        for(ChatParticipant c : chatParticipants){
            if(c.getMember().equals(member)){
                return true;
            }
        }

        return false;
    }

    // 메시지 상태를 모두 read로 바꾸기
    public void messageRead(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room can not be found"));
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member can not be found"));
        List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndMember(chatRoom,member);

        for (ReadStatus r : readStatuses) {
            r.updateIsRead(true);

            readStatusRepository.save(r);
        }
    }

    // 내 채팅방 목록 가져오기
    public List<MyChatResListDto> getMyChatRooms() {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member can not be found"));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMember(member);
        List<MyChatResListDto> dtos = new ArrayList<>();

        for (ChatParticipant c : chatParticipants) {
            Long count = readStatusRepository.countByChatRoomAndMemberAndIsReadFalse(c.getChatRoom(), member);


            MyChatResListDto dto = MyChatResListDto.builder()
                    .roomId(c.getChatRoom().getId())
                    .roomName(c.getChatRoom().getName())
                    .isGroupChat(c.getChatRoom().getIsGroupChat())
                    .unReadCount(count)
                    .build();

            dtos.add(dto);
        }

        return dtos;
    }

    // group 채팅방 나가기 -> chatParticipant 객체만 삭제
    // 내가 나가고 참여자가 없으면 채팅방, 메시지 모두 삭제
    public void leaveGroupChatRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room can not be found"));
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member can not be found"));

        if(chatRoom.getIsGroupChat().equals("N")){
            throw new IllegalArgumentException("단체 채팅방이 아닙니다.");
        }

        ChatParticipant c = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member).orElseThrow(() -> new EntityNotFoundException("참여자를 찾을수 없습니다."));
        chatParticipantRepository.delete(c);

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        if(chatParticipants.isEmpty()){
            chatRoomRepository.delete(chatRoom); // chatMessage 삭제 -> readStatus 삭제 (cascade option)
        }
    }

    // 개인 채팅방 개설 or 기존 룸 아이디 return (chatParticipant table 조회 필요)
    public Long getOrCreatePrivateRoom(Long otherMemberId) {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member can not be found"));
        Member otherMember = memberRepository.findById(otherMemberId).orElseThrow(() -> new EntityNotFoundException("member can not be found"));

        // 나와 상대방이 1:1 채팅에 이미 참여하고 있다면 해당 RoomId return
        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingPrivateRoom(member.getId(), otherMember.getId());
        if(chatRoom.isPresent()){
            return chatRoom.get().getId();
        }

        // 만약 1:1 채팅방이 없을 경우 1:1 채팅방 개설
        ChatRoom newRoom = ChatRoom.builder()
                .name(member.getName() + "-" + otherMember.getName())
                .isGroupChat("N")
                .build();

        chatRoomRepository.save(newRoom); // -> id값 세팅

        // 두 사람 모두 참여자로 새롭게 추가
        addParticipantToRoom(newRoom, member);
        addParticipantToRoom(newRoom, otherMember);

        return newRoom.getId();
    }
}
