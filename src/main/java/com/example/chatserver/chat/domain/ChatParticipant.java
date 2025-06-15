package com.example.chatserver.chat.domain;

import com.example.chatserver.common.domain.BaseTimeEntity;
import com.example.chatserver.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChatParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 성능 문제
    @JoinColumn(name="chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY) // 성능 문제
    @JoinColumn(name="member_id", nullable = false)
    private Member member;
}

/**
 * FetchType.LAZY
 * 관계성을 가지고 해당 엔티티를 조회할때 FK로 엮인 부분도 같이 조회할 것인지
 * default = FetchType.EAGER -> 한꺼번에 모두 조회
 * LAZY -> 해당 칼럼을 사용할때 조회
 */
