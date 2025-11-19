package com.hung.chatbot.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationHistoryDTO {
    private Long conversationId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private List<MessageDTO> messages;
}