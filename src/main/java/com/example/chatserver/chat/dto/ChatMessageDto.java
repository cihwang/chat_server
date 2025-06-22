package com.example.chatserver.chat.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChatMessageDto {
    private String message;
    private String senderEmail;
}
