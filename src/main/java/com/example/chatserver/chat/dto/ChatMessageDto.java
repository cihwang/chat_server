package com.example.chatserver.chat.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private long roomId;
    private String message;
    private String senderEmail;
}
