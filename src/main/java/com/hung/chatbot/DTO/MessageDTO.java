package com.hung.chatbot.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long messageId;
    private String sender;  // "user" | "bot"
    private String content;
    private LocalDateTime createdAt;
}