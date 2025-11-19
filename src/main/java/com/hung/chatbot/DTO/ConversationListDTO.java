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
public class ConversationListDTO {
    private Long conversationId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private int messageCount;
}