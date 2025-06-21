package com.example.chatserver.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// roomId, roomName, isGroupChat(groupChat만 나가기 기능활성화), messageReadCnt
public class MyChatResListDto {
    private Long roomId;
    private String roomName;
    private String isGroupChat;
    private Long unReadCount;
}
