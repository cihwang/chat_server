package com.example.chatserver.chat.repository;

import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    Optional<ChatParticipant> findByChatRoomAndMember(ChatRoom chatRoom, Member member);

    // 내가 참여하고있는 모든 ChatParticipant 가져오기
    List<ChatParticipant> findAllByMember(Member member);

    @Query("SELECT cp1.chatRoom " +
            "FROM ChatParticipant cp1 " +
            "JOIN ChatParticipant cp2 " +
            "ON cp1.chatRoom.id = cp2.chatRoom.id " +
            "WHERE cp1.member.id = :myId " +
            "AND cp2.member.id = :otherMemberId " +
            "AND cp1.chatRoom.isGroupChat = 'N'")
    Optional<ChatRoom> findExistingPrivateRoom(@Param("myId") Long myId, @Param("otherMemberId") Long otherMemberId);
}
