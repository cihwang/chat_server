package com.example.chatserver.chat.repository;

import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {

    List<ReadStatus> findByChatRoomAndMember(ChatRoom chatRoom, Member member);

    // 읽지 않은 메시지 개수 가져오기
    Long countByChatRoomAndMemberAndIsReadFalse(ChatRoom chatRoom, Member member);
}
