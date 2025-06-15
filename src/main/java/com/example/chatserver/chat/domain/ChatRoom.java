package com.example.chatserver.chat.domain;

import com.example.chatserver.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 채팅방 이름

    @Builder.Default
    private String isGroupChat = "N"; // 1:N or 1:1

    /**
     * mappedBy -> oneToMany로 걸려있는 속성 명을 쓰는 것
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE)
    private List<ChatParticipant> chatParticipant = new ArrayList<>();

    /**
     * ReadStatus가 같이 삭제되도록 설정
     * orphanRemoval = true -> chatParticipant를 삭제할때 해당 chatParticipant를 참조하는 모든 정보 삭제
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();
}
